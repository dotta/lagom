/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.javadsl.broker.kafka

import com.google.inject.AbstractModule
import com.lightbend.lagom.internal.broker.SessionProvider
import com.lightbend.lagom.internal.broker.kafka._
import com.lightbend.lagom.javadsl.api.broker.Topics

class KafkaModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[SessionProvider]).to(classOf[KafkaSessionProvider])
    bind(classOf[ZooKeeperConfig]).to(classOf[ZooKeeperConfigImpl])
    bind(classOf[KafkaConfig]).to(classOf[KafkaConfigImpl])
    bind(classOf[Topics]).to(classOf[KafkaTopics])
  }

}
