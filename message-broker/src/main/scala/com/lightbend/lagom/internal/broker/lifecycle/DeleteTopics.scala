/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.broker.lifecycle

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

import org.slf4j.LoggerFactory

import com.lightbend.lagom.internal.broker.SessionProvider
import com.lightbend.lagom.internal.server.ResolvedServices

import akka.Done

import javax.inject.Inject

class DeleteTopics @Inject() (resolvedServices: ResolvedServices, sessionProvider: SessionProvider)(implicit ec: ExecutionContext) {
  private val log = LoggerFactory.getLogger(classOf[DeleteTopics])

  def execute(): Future[Done] = {
    val topicIds = for {
      service <- resolvedServices.services
      topicCall <- service.descriptor.topicCalls().asScala
    } yield topicCall.topicId

    val session = sessionProvider.createSession()
    val deletedTopics = topicIds.map { topicId =>
      session.deleteTopic(topicId) andThen {
        case Failure(ex: Throwable) => log.warn(s"Failed to delete topic $topicId.", ex)
        case Success(_)             => log.debug(s"Successfully deleted topic $topicId.")
      }
    }
    Future.sequence(deletedTopics).map(_ => Done) andThen {
      case _ => session.close()
    }
  }
}
