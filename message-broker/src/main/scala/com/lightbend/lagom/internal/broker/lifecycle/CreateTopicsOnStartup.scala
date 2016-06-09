/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.broker.lifecycle

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CreateTopicsOnStartup @Inject() (config: CreateTopicsOnStartup.Config, createTopics: CreateTopics)(implicit ec: ExecutionContext) {
  @volatile private var topicsCreated = !config.createTopicsOnStartup

  // FIXME: There is an issue with how topics are currently initialized. Because the topics are initialized asynchronously
  //        (i.e., the initialization logic is run within a Future), there is a risk that a service is started before the 
  //        kafka topics have been created, and that could cause a failure in the service if it attempts to publish messages 
  //        to a topic. A similar problem could exist if the Kafka brokers are down or unresponsive, and this could be an 
  //        issue especially when running in production. It turns out we have the same potential issue with Cassandra, and
  //        in fact we have already discussed the idea of having an actor taking care of re-trying to execute an action 
  //        (e.g., a Cassandra query) in https://github.com/lagom/lagom/pull/130. I'm wondering if a similar abstraction
  //        should be used here to avoid attempting to push messages to Kafka when the brokers are not available.

  if (config.createTopicsOnStartup) {
    createTopics.execute() andThen {
      case _ => topicsCreated = true
    }
  }

  def areTopicsCreated: Boolean = topicsCreated
}

object CreateTopicsOnStartup {
  trait Config {
    def createTopicsOnStartup: Boolean
  }

  class ConfigImpl @Inject() (config: play.api.Configuration) extends Config {
    def createTopicsOnStartup: Boolean = config.underlying.getBoolean("lagom.broker.create-topics-on-startup")
  }
}
