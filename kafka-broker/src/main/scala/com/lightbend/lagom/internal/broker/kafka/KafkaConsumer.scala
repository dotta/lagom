/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.broker.kafka

import java.lang.reflect.Type
import java.util.Optional
import java.util.concurrent.atomic.AtomicLong

import scala.collection.concurrent
import scala.util.{ Success, Failure }

import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory

import com.fasterxml.jackson.databind.`type`.TypeFactory
import com.lightbend.lagom.internal.jackson.JacksonObjectMapperProvider
import com.lightbend.lagom.javadsl.api.ServiceInfo
import com.lightbend.lagom.javadsl.api.broker.Subscriber
import com.lightbend.lagom.javadsl.api.broker.Topic.TopicId

import akka.Done
import akka.actor.ActorSystem
import akka.kafka._
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.scaladsl.Consumer
import akka.stream.FlowShape
import akka.stream.javadsl.{ Flow => JFlow, Source => JSource }
import akka.stream.scaladsl.{ Flow, GraphDSL, Unzip, Zip }

class KafkaConsumer[Message](config: KafkaConfig, topicId: TopicId, messageType: Type, override val group: Optional[Subscriber.Group], info: ServiceInfo)(implicit system: ActorSystem) extends Subscriber[Message] {

  private val log = LoggerFactory.getLogger(classOf[KafkaConsumer[_]])

  private val objectMapper = JacksonObjectMapperProvider.get(system).objectMapper(messageType)

  private val jacksonType = TypeFactory.defaultInstance().constructType(messageType)

  private val consumerSettings = {
    val topicConfig = config.topicConfigOf(topicId)
    val messageKeyDeserializer = topicConfig.messageKeyDeserializer[Array[Byte]]
    val messageValueDeserializer = topicConfig.messageValueDeserializer[String]
    val clientId = info.serviceName()

    val settings = ConsumerSettings(system, messageKeyDeserializer, messageValueDeserializer)
      .withBootstrapServers(config.brokers)
      .withClientId(clientId)
      .withProperty(org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    if (group.isPresent()) settings.withGroupId(group.get.groupId())
    else {
      // An empty group id is not allowed by Kafka (see https://issues.apache.org/jira/browse/KAFKA-2648 and https://github.com/akka/reactive-kafka/issues/155), 
      // hence we are automatically generating one.

      // FIXME: Users should be able to control enabling/disabling of this feature for automatically creating a group id.
      // I'm actually thinking that by default we should not automatically create a group id (throwing an exception should be the default) 
      val freshGroupId = KafkaConsumer.Group.obtainFreshGroupId(clientId)
      log.info(s"A Kafka Consumer without group id is not allowed (see KAFKA-2648 for details). A unique [groupId=$freshGroupId] is automatically generated and used.")
      settings.withGroupId(freshGroupId)
    }
  }

  override def atMostOnceSource: JSource[Message, _] = {
    Consumer.atMostOnceSource(consumerSettings, Subscriptions.topics(topicId.value))
      .map(rawMessage => {
        // casting because otherwise the expression has type Nothing inferred
        val parsedMessage = objectMapper.readValue(rawMessage.value, jacksonType).asInstanceOf[Message]
        parsedMessage
      })
      .asJava
  }

  override def atLeastOnceSource(flow: JFlow[Message, Message, _]): JSource[Done, _] = {
    val committableSource = Consumer.committableSource(consumerSettings, Subscriptions.topics(topicId.value))
      .map(rawMessage => {
        // casting because otherwise the expression has type Nothing inferred
        val parsedMessage = objectMapper.readValue(rawMessage.value, jacksonType).asInstanceOf[Message]
        (rawMessage, parsedMessage)
      })

    val committOffsetFlow = Flow.fromGraph(GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._
      val unzip = builder.add(Unzip[CommittableMessage[_, _], Message])
      val zip = builder.add(Zip[CommittableMessage[_, _], Message])
      // parallelism set to 1 because offset should be committed in order
      val committer = Flow[(CommittableMessage[_, _], Message)].mapAsync(parallelism = 1) {
        case (cm, _) =>
          implicit val ec = system.dispatcher
          cm.committableOffset.commitScaladsl() andThen {
            // logging is done here (instead than in a separated flow stage) because we want 
            // the log message to be displayed also when a failure occurs. Note that because
            // of the `mapAsync` semantic, the log message would NOT be displayed if done in
            // a separate, downstream, stage.
            case Success(_) =>
              log.info(s"Successfully committed offset ${cm.committableOffset.partitionOffset}.")
            case Failure(err: Throwable) =>
              log.warn(s"Failed to commit offset ${cm.committableOffset.partitionOffset}. Unless you have defined a custom supervision strategy, this stream is going to be completed with a failure.", err)
          }
      }

      unzip.out0 ~> zip.in0
      unzip.out1 ~> flow ~> zip.in1
      zip.out ~> committer

      FlowShape(unzip.in, committer.shape.out)
    })

    committableSource.via(committOffsetFlow).asJava
  }

  override def withGroupId(groupId: String): Subscriber[Message] = {
    val group: Optional[Subscriber.Group] = {
      if (groupId == null) Optional.empty()
      else Optional.of(KafkaConsumer.Group(groupId))
    }
    new KafkaConsumer(config, topicId, messageType, group, info)
  }
}

object KafkaConsumer {
  case class Group(groupId: String) extends Subscriber.Group {
    require(Group.validateGroupId(groupId), s"Failed to create group because [groupId=$groupId] contains invalid character(s). Check the Kafka spec for creating a valid group id.")
  }
  case object Group {

    private val InvalidGroupIdChars = Set('/', '\\', ',', '\u0000', ':', '"', '\'', ';', '*', '?', ' ', '\t', '\r', '\n', '=')
    // based on https://github.com/apache/kafka/blob/623ab1e7c6497c000bc9c9978637f20542a3191c/core/src/test/scala/unit/kafka/common/ConfigTest.scala#L60
    private def validateGroupId(groupId: String): Boolean = !groupId.exists { InvalidGroupIdChars.apply }

    private[this] case class ClientId(id: String) extends AnyVal

    // FIXME: Need to think more about the logic for creating fresh group ids (maybe it's overkill?)
    private[this] val FreshGroupIds: concurrent.Map[ClientId, AtomicLong] = concurrent.TrieMap.empty

    private[KafkaConsumer] def obtainFreshGroupId(clientId: String): String = {
      @annotation.tailrec
      def loop(clientId: ClientId): String = {
        def freshGroupId(counter: AtomicLong): String = s"${clientId.id}-group${counter.get}"
        FreshGroupIds.get(clientId) match {
          case None =>
            val counter = new AtomicLong(0)
            FreshGroupIds.putIfAbsent(clientId, counter) match {
              case None           => freshGroupId(counter) // insertion OK!
              case Some(oldValue) => loop(clientId) // there was a race condition, let's start over
            }
          case Some(counter) =>
            val newCounter = new AtomicLong(counter.incrementAndGet())
            if (FreshGroupIds.replace(clientId, counter, newCounter)) freshGroupId(newCounter)
            else loop(clientId) // there was a race condition, let's start over
        }
      }

      loop(ClientId(clientId))
    }

  }
}
