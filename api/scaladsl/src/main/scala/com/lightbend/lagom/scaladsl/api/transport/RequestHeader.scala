/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.scaladsl.api.transport

import com.lightbend.lagom.internal.api.transport.CoreRequestHeader
import scala.collection.immutable.Seq
import com.lightbend.lagom.api.transport.Method
import java.net.URI
import java.security.Principal

final class RequestHeader private[lagom] (private[lagom] val delegate: CoreRequestHeader) extends MessageHeader(delegate) {

  /**
   * Get the method used to make this request.
   *
   * @return The method.
   */
  def method: Method = delegate.method

  /**
   * Get the URI for this request.
   *
   * @return The URI.
   */
  def uri: URI = delegate.uri

  /**
   * Get the accepted response protocols for this request.
   *
   * @return The accepted response protocols.
   */
  def acceptedResponseProtocols: Seq[MessageProtocol] =
    delegate.acceptedResponseProtocols.map(new MessageProtocol(_))

  /**
   * Get the principal for this request, if there is one.
   *
   * @return The principal for this request.
   */
  def principal: Option[Principal] = delegate.principal

  /**
   * Return a copy of this request header with the given method set.
   *
   * @param method
   *          The method to set.
   * @return A copy of this request header.
   */
  def withMethod(method: Method): RequestHeader =
    new RequestHeader(delegate.withMethod(method))

  /**
   * Return a copy of this request header with the given uri set.
   *
   * @param uri
   *          The uri to set.
   * @return A copy of this request header.
   */
  def withUri(uri: URI): RequestHeader = new RequestHeader(delegate.withUri(uri))

  override def withProtocol(protocol: MessageProtocol): RequestHeader =
    new RequestHeader(delegate.withProtocol(protocol.delegate))

  /**
   * Return a copy of this request header with the given accepted response
   * protocols set.
   *
   * @param acceptedResponseProtocols
   *          The accepted response protocols to set.
   * @return A copy of this request header.
   */
  def withAcceptedResponseProtocols(acceptedResponseProtocols: Seq[MessageProtocol]): RequestHeader = {
    val adapted = acceptedResponseProtocols.map(_.delegate)
    new RequestHeader(delegate.withAcceptedResponseProtocols(adapted))
  }

  /**
   * Return a copy of this request header with the principal set.
   *
   * @param principal
   *          The principal to set.
   * @return A copy of this request header.
   */
  def withPrincipal(principal: Principal): RequestHeader =
    new RequestHeader(delegate.withPrincipal(principal))

  /**
   * Return a copy of this request header with the principal cleared.
   *
   * @return A copy of this request header.
   */
  def withNoPrincipal: RequestHeader = withPrincipal(null)

  override def replaceAllHeaders(headers: Map[String, Seq[String]]): RequestHeader =
    new RequestHeader(delegate.replaceAllHeaders(headers))

  override def withHeader(name: String, value: String): RequestHeader =
    new RequestHeader(delegate.withHeader(name, value))

  override def equals(that: Any): Boolean = {
    if (this == 0) true
    else that match {
      case that: RequestHeader => delegate == that.delegate
      case _                   => false
    }
  }

}

object RequestHeader {
  /**
   * A default request header object.
   *
   * This is a convenience supplied so that server implementations of service
   * calls can pass this request to the request header handler, in order to get
   * the actual incoming request header.
   *
   * See
   * {@link com.lightbend.lagom.javadsl.api.ServiceCall#handleRequestHeader(Function)}
   */
  val Default: RequestHeader = new RequestHeader(CoreRequestHeader.Default)

  def apply(method: Method, uri: URI, protocol: MessageProtocol,
            acceptedResponseProtocols: Seq[MessageProtocol], principal: Option[Principal],
            headers: Map[String, Seq[String]]): RequestHeader = new RequestHeader(new CoreRequestHeader(method, uri, protocol.delegate, acceptedResponseProtocols.map(_.delegate), principal, headers))
}
