/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.broker.lifecycle

import scala.collection.JavaConversions.asScalaBuffer

import org.slf4j.LoggerFactory

import com.lightbend.lagom.internal.api.MethodTopicHolder
import com.lightbend.lagom.internal.server.ResolvedServices
import com.lightbend.lagom.javadsl.api.Service.TopicSource
import com.lightbend.lagom.javadsl.api.broker.Topics

import javax.inject.Inject

class RegisterTopicsPublishers @Inject() (createTopicsOnStartup: CreateTopicsOnStartup, resolvedServices: ResolvedServices, topics: Topics) {

  private val log = LoggerFactory.getLogger(classOf[RegisterTopicsPublishers])

  while (!createTopicsOnStartup.areTopicsCreated) () // busy spin

  for {
    service <- resolvedServices.services
    topicCall <- service.descriptor.topicCalls().toSeq
  } yield topicCall.topicCallHolder() match {
    case holder: MethodTopicHolder =>
      val topic = holder.create(service.service).asInstanceOf[TopicSource[AnyRef]]
      val topicId = topic.topicId
      val topicImpl = topics.of[AnyRef](topicId, classOf[AnyRef])
      log.debug(s"Registering publisher source for topic $topicId.")
      topicImpl.publisher().publish(topic.messages())

    case _ => ()
  }
}
