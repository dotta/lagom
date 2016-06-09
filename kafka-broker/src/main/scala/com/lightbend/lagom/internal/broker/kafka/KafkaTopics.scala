/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.broker.kafka

import java.lang.reflect.Type

import com.lightbend.lagom.javadsl.api.ServiceInfo
import com.lightbend.lagom.javadsl.api.broker.Topic
import com.lightbend.lagom.javadsl.api.broker.Topics
import com.lightbend.lagom.javadsl.api.broker.Topic.TopicId

import akka.actor.ActorSystem
import akka.stream.Materializer
import javax.inject.Inject

class KafkaTopics @Inject() (config: KafkaConfig, info: ServiceInfo, system: ActorSystem)(implicit mat: Materializer) extends Topics {
  override def of[Message](topicId: TopicId, messageType: Type): Topic[Message] = {
    KafkaTopic[Message](config, topicId, messageType, info, system)
  }
}
