/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api.deser

import scala.collection.immutable.Seq
import com.lightbend.lagom.internal.api.transport.CoreMessageProtocol

trait CoreExceptionSerializer {
  def serialize(exception: Throwable, accept: Seq[CoreMessageProtocol]): CoreRawExceptionMessage
  def deserialize(message: CoreRawExceptionMessage): Throwable
}

object CoreExceptionSerializer {
  object PlaceholderExceptionSerializer extends CoreExceptionSerializer {
    override def serialize(exception: Throwable, accept: Seq[CoreMessageProtocol]): CoreRawExceptionMessage =
      throw new UnsupportedOperationException("This serializer is only a placeholder, and cannot be used directly: " + this, exception)

    override def deserialize(message: CoreRawExceptionMessage): Throwable =
      throw new UnsupportedOperationException("This serializer is only a placeholder, and cannot be used directly: " + this + ", trying te deserialize: " + message)
  }
}
