/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.scaladsl.api.deser

import com.lightbend.lagom.scaladsl.api.transport.MessageProtocol

trait ExceptionSerializer {

  def serialize(exception: Throwable, accept: Seq[MessageProtocol]): RawExceptionMessage

  def deserialize(message: RawExceptionMessage): Throwable
}
