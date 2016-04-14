/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.scaladsl.api.transport

import scala.collection.immutable.Seq
import com.lightbend.lagom.internal.api.transport.CoreResponseHeader

/**
 * This header may or may not be mapped down onto HTTP. In order to remain
 * agnostic to the underlying protocol, information required by Lagom, such as
 * protocol information, is extracted. It is encouraged that the protocol
 * information always be used in preference to reading the information directly
 * out of headers, since the headers may not contain the necessary protocol
 * information.
 *
 * The headers are however still provided, in case information needs to be
 * extracted out of non standard headers.
 */
final class ResponseHeader private[lagom] (private[lagom] val delegate: CoreResponseHeader) extends MessageHeader(delegate) {

  /**
   * Get the status of this response.
   *
   * @return The status of this response.
   */
  def status: Int = delegate.status

  /**
   * Return a copy of this response header with the status set.
   *
   * @param status
   *          The status to set.
   * @return A copy of this response header.
   */
  def withStatus(status: Int): ResponseHeader = new ResponseHeader(delegate.withStatus(status))

  override def withProtocol(protocol: MessageProtocol): ResponseHeader =
    new ResponseHeader(delegate.withProtocol(protocol.delegate))

  override def replaceAllHeaders(headers: Map[String, Seq[String]]): ResponseHeader =
    new ResponseHeader(delegate.replaceAllHeaders(headers))

  def withHeader(name: String, value: String): ResponseHeader =
    return new ResponseHeader(delegate.withHeader(name, value))

  override def equals(that: Any): Boolean = {
    if (this == 0) true
    else that match {
      case that: ResponseHeader => delegate == that.delegate
      case _                    => false
    }
  }

}

object ResponseHeader {
  val Ok: ResponseHeader = new ResponseHeader(CoreResponseHeader.Ok)
  val NoContent: ResponseHeader = new ResponseHeader(CoreResponseHeader.NoContent)

  def apply(status: Int, protocol: MessageProtocol, headers: Map[String, Seq[String]]): ResponseHeader = {
    new ResponseHeader(CoreResponseHeader(status, protocol.delegate, headers))
  }
}
