/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api.deser

import com.lightbend.lagom.api.transport.TransportErrorCode
import com.lightbend.lagom.internal.api.transport.CoreMessageProtocol
import akka.util.ByteString
import java.util.Base64
import scala.reflect.ClassTag

case class CoreRawExceptionMessage(errorCode: TransportErrorCode, protocol: CoreMessageProtocol, message: ByteString) {
  def messageAsText: String = protocol.charset match {
    case Some(charset) => message.decodeString(charset)
    case None =>
      val ev = implicitly[ClassTag[Byte]]
      Base64.getEncoder().encodeToString(message.toArray(ev))
  }
}
