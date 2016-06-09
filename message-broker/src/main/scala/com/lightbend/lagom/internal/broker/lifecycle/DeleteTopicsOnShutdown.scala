/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.broker.lifecycle

import javax.inject.{ Inject, Singleton }
import play.api.inject.ApplicationLifecycle

@Singleton
class DeleteTopicsOnShutdown @Inject() (config: DeleteTopicsOnShutdown.Config, lifecycle: ApplicationLifecycle, deleteTopics: DeleteTopics) {
  if (config.deleteTopicsOnShutdown)
    lifecycle.addStopHook { () => deleteTopics.execute() }
}

object DeleteTopicsOnShutdown {
  trait Config {
    def deleteTopicsOnShutdown: Boolean
  }

  class ConfigImpl @Inject() (config: play.api.Configuration) extends Config {
    def deleteTopicsOnShutdown: Boolean = config.underlying.getBoolean("lagom.broker.delete-topics-on-shutdown")
  }
}
