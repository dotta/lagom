/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.scaladsl.api.transport

import java.util.regex.Pattern

import com.lightbend.lagom.internal.api.transport.CorePathVersionedProtocolNegotiationStrategy
import com.lightbend.lagom.internal.api.transport.DelegatingHeaderTransformer

/**
 * Negotiates the protocol using versions from the path.
 */
object PathVersionedProtocolNegotiationStrategy {
  def apply(): HeaderTransformer = new DelegatingHeaderTransformer(CorePathVersionedProtocolNegotiationStrategy.apply())

  def apply(pathVersionExtractor: Pattern, pathVersionFormat: String): HeaderTransformer =
    new DelegatingHeaderTransformer(new CorePathVersionedProtocolNegotiationStrategy(
      pathVersionExtractor,
      pathVersionFormat
    ))
}