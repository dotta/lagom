/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.broker.kafka

import java.lang.reflect.Type

import java.util.Optional

import com.lightbend.lagom.javadsl.api.ServiceInfo
import com.lightbend.lagom.javadsl.api.broker.Publisher
import com.lightbend.lagom.javadsl.api.broker.Subscriber
import com.lightbend.lagom.javadsl.api.broker.Topic
import com.lightbend.lagom.javadsl.api.broker.Topic.TopicId

import akka.actor.ActorSystem
import akka.stream.Materializer

case class KafkaTopic[Message](config: KafkaConfig, topicId: TopicId, messageType: Type, info: ServiceInfo, system: ActorSystem)(implicit mat: Materializer) extends Topic[Message] {

  override def subscribe(): Subscriber[Message] = new KafkaConsumer(config, topicId, messageType, group = Optional.empty(), info)(system)

  override def publisher(): Publisher[Message] = new KafkaPublisher(config, topicId, messageType, system)
}
