/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.scaladsl.api

import java.net.URI
import scala.concurrent.Future

/**
 * Locates services.
 */
trait ServiceLocator {

  /**
   * Locate a service with the given name.
   *
   * @param name The name of the service.
   * @return The URI for that service, if it exists.
   */
  def locate(name: String): Future[Option[URI]]

  /**
   * Do the given action with the given service.
   *
   * This should be used in preference to {@link #locate(String)} when possible as it will allow the service
   * locator update its caches in case there's a problem with the service it returned.
   *
   * @param name The name of the service.
   * @param block A block of code that takes the URI for the service, and returns a future of some work done on the
   *              service. This will only be executed if the service was found.
   * @return The result of the executed block, if the service was found.
   */
  def doWithService[T](name: String)(block: URI => Future[T]): Future[Option[T]]
}
