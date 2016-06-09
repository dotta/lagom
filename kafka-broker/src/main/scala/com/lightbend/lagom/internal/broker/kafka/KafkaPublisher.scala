/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.broker.kafka

import java.lang.reflect.Type

import org.apache.kafka.clients.producer.ProducerRecord

import com.lightbend.lagom.internal.jackson.JacksonObjectMapperProvider
import com.lightbend.lagom.javadsl.api.broker.Publisher
import com.lightbend.lagom.javadsl.api.broker.Topic.TopicId
import com.lightbend.lagom.javadsl.broker.kafka.message.PartitioningStrategy

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.Materializer
import akka.stream.javadsl.{ Source => JSource }
import com.lightbend.lagom.javadsl.broker.kafka.message.{HasKey, HasPartition} 
import java.util.OptionalInt

class KafkaPublisher[Message](config: KafkaConfig, topicId: TopicId, messageType: Type, system: ActorSystem, partitioningStrategy: PartitioningStrategy = KafkaPublisher.DefaultPartitioningStrategy)(implicit mat: Materializer) extends Publisher[Message] {

  private val mapper = {
    val objectMapperProvider = JacksonObjectMapperProvider.get(system)
    objectMapperProvider.objectMapper(messageType)
  }

  private val producerSettings = {
    val topicConfig = config.topicConfigOf(topicId)
    // FIXME: The type to cast to should (somehow) be injected 
    val messageKeySerializer = topicConfig.messageKeySerializer[Array[Byte]]
    val messageValueSerializer = topicConfig.messageValueSerializer[String]

    ProducerSettings(system, messageKeySerializer, messageValueSerializer)
    .withBootstrapServers(config.brokers)
  }

  override def publish(messages: JSource[Message, _]): Unit = {
    messages.asScala
      .map { message =>
        val serializedMessage = mapper.writeValueAsString(message)
        val partition = partitioningStrategy.partitionOf(message)
        val key = message match {
          case m: HasKey[Array[Byte]] => m.key
          case _ => null
        }
        new ProducerRecord[Array[Byte], String](topicId.value, partition, key, serializedMessage)
      }.to(Producer.plainSink(producerSettings)).run()
  }

  def withPartitioningStrategy(strategy: PartitioningStrategy): KafkaPublisher[Message] =
    new KafkaPublisher(config, topicId, messageType, system, strategy)
}

object KafkaPublisher {
  object DefaultPartitioningStrategy extends PartitioningStrategy {
    def partitionOf(message: Any): Integer = message match {
      case m: HasPartition => m.partition()
      case _ => null
    }
  }
}