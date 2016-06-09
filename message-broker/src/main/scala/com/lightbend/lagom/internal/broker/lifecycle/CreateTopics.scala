/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.broker.lifecycle

import scala.collection.JavaConversions.asScalaBuffer
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

import org.slf4j.LoggerFactory

import com.lightbend.lagom.internal.api.MethodTopicHolder
import com.lightbend.lagom.internal.broker.SessionProvider
import com.lightbend.lagom.internal.server.ResolvedServices
import com.lightbend.lagom.javadsl.api.Service.TopicSource
import com.lightbend.lagom.javadsl.api.broker.Topics

import javax.inject.Inject
import akka.Done

class CreateTopics @Inject() (resolvedServices: ResolvedServices, sessionProvider: SessionProvider, topics: Topics)(implicit ec: ExecutionContext) {

  private val log = LoggerFactory.getLogger(classOf[CreateTopics])

  // FIXME: There is an issue with how topics are currently created. Because the topics are initialized asynchronously
  //        (i.e., the initialization logic is run within a Future), there is a risk that a service is started before the 
  //        kafka topics have been created, and that could cause a failure in the service if it attempts to publish messages 
  //        to a topic. A similar problem could exist if the Kafka brokers are down or unresponsive, and this could be an 
  //        issue especially when running in production. It turns out we have the same potential issue with Cassandra, and
  //        in fact we have already discussed the idea of having an actor taking care of re-trying to execute an action 
  //        (e.g., a Cassandra query) in https://github.com/lagom/lagom/pull/130. I'm wondering if a similar abstraction
  //        should be used here to avoid attempting to push messages to Kafka when the brokers are not available.

  def execute(): Future[Done] = {
    val session = sessionProvider.createSession()
    val topicsInitialized = for {
      service <- resolvedServices.services
      topicCall <- service.descriptor.topicCalls().toSeq
    } yield topicCall.topicCallHolder() match {
      case holder: MethodTopicHolder =>
        val topic = holder.create(service.service).asInstanceOf[TopicSource[AnyRef]]
        val topicId = topic.topicId
        session.newTopic(topicId) andThen {
          case Failure(ex: Throwable) =>
            log.error(s"Failed to create topic $topicId")
          case Success(_) =>
            log.debug(s"Successfully created topic $topicId")
        }

      case holder =>
        val msg = s"Expected Topic call holder of type ${classOf[MethodTopicHolder].getName}, but found ${holder.getClass.getName}"
        throw new IllegalStateException(msg)
    }

    Future.sequence(topicsInitialized).map(_ => Done) andThen {
      case _ => session.close()
    }
  }
}
