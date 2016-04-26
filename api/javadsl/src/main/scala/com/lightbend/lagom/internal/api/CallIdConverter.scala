/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api

import com.lightbend.lagom.javadsl.api.Descriptor

object CallIdConverter {

  def asApi(callId: CoreDescriptor.CallId): Descriptor.CallId = callId match {
    case CoreDescriptor.RestCallId(method, pathPattern) => new Descriptor.RestCallId(method, pathPattern)
    case CoreDescriptor.PathCallId(pathPattern) => new Descriptor.PathCallId(pathPattern)
    case CoreDescriptor.NamedCallId(name) => new Descriptor.NamedCallId(name)
    case _ => reportBug(callId)
  }

  private def reportBug(callId: Any): Nothing =
    throw new IllegalArgumentException(s"Cannot convert call id type ${callId.getClass}. This is a bug, please report it.")

}
