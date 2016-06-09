/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.broker

import java.io.Closeable
import java.lang.reflect.Type

import com.lightbend.lagom.javadsl.api.broker.Topic.TopicId

import akka.stream.javadsl.Source
import scala.concurrent.Future
import akka.Done

trait SessionProvider {
  def createSession(): SessionProvider.Session
}

object SessionProvider {
  trait Session extends Closeable {
    def newTopic(topicId: TopicId): Future[Done]
    def deleteTopic(topicId: TopicId): Future[Done]
  }
}
