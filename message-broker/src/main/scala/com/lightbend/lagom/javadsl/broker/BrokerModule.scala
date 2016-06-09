/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.javadsl.broker

import com.google.inject.AbstractModule
import com.lightbend.lagom.internal.broker.lifecycle._

class BrokerModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[CreateTopicsOnStartup.Config]).to(classOf[CreateTopicsOnStartup.ConfigImpl])
    bind(classOf[DeleteTopicsOnShutdown.Config]).to(classOf[DeleteTopicsOnShutdown.ConfigImpl])
    bind(classOf[DeleteTopicsOnShutdown]).asEagerSingleton()
    bind(classOf[CreateTopicsOnStartup]).asEagerSingleton()
    bind(classOf[RegisterTopicsPublishers]).asEagerSingleton()
  }

}
