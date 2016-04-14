/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.scaladsl.api

import com.lightbend.lagom.internal.api.CoreServiceAcl
import com.lightbend.lagom.api.transport.Method

final class ServiceAcl private (delegate: CoreServiceAcl) {
  def method: Option[Method] = delegate.method
  def pathRegex: Option[String] = delegate.pathRegex
}

object ServiceAcl {
  def apply(method: Option[Method], pathRegex: Option[String]): ServiceAcl = {
    val delegate = CoreServiceAcl(method, pathRegex)
    new ServiceAcl(delegate)
  }
}
