/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.scaladsl.api.deser

import java.util.Base64

import scala.reflect.ClassTag

import com.lightbend.lagom.api.transport.TransportErrorCode
import com.lightbend.lagom.scaladsl.api.transport.MessageProtocol

import akka.util.ByteString
import com.lightbend.lagom.internal.api.deser.CoreRawExceptionMessage

/**
 * A serialized exception message.
 */
class RawExceptionMessage private (delegate: CoreRawExceptionMessage) {

  def errorCode: TransportErrorCode = delegate.errorCode
  def protocol: MessageProtocol = MessageProtocol(delegate.protocol.contentType, delegate.protocol.charset, delegate.protocol.version)
  def message: ByteString = delegate.message

  /**
   * Get the message as text.
   *
   * If this is a binary message (that is, the message protocol does not define a charset), encodes it using Base64.
   *
   * @return The message as text.
   */
  def messageAsText: String = delegate.messageAsText
}

object RawExceptionMessage {
  def apply(errorCode: TransportErrorCode, protocol: MessageProtocol, message: ByteString): RawExceptionMessage = {
    val delegate = CoreRawExceptionMessage(errorCode, protocol.delegate, message)
    new RawExceptionMessage(delegate)
  }

}
