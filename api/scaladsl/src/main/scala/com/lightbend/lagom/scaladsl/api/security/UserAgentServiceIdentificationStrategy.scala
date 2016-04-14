/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.scaladsl.api.security

import com.lightbend.lagom.internal.api.security.CoreUserAgentServiceIdentificationStrategy
import com.lightbend.lagom.internal.api.transport.DelegatingHeaderTransformer
import com.lightbend.lagom.scaladsl.api.transport.HeaderTransformer

/**
 * Transfers service principal information via the <tt>User-Agent</tt> header.
 *
 * If using this on a service that serves requests from the outside world, it
 * would be a good idea to block the <tt>User-Agent</tt> header in the web
 * facing load balancer/proxy.
 */
object UserAgentServiceIdentificationStrategy {
  def apply(): HeaderTransformer = new DelegatingHeaderTransformer(new CoreUserAgentServiceIdentificationStrategy)
}