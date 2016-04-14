/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.scaladsl.api.transport

import com.lightbend.lagom.internal.api.transport.CoreMessageProtocol
import com.lightbend.lagom.api.transport.GenericMessageProtocol

/**
 * A message protocol.
 *
 * This describes the negotiated protocol being used for a message.  It has three elements, a content type, a charset,
 * and a version.
 *
 * The <tt>contentType</tt> may be registered mime type such as <tt>application/json</tt>, or it could be an application
 * specific content type, such as <tt>application/vnd.myservice+json</tt>.  It could also contain protocol versioning
 * information, such as <tt>application/vnd.github.v3+json</tt>.  During the protocol negotiation process, the
 * content type may be transformed, for example, if the content type contains a version, the configured
 * {@link HeaderTransformer} will be expected to extract that version out into the <tt>version</tt>, leaving a
 * <tt>contentType</tt> that will be understood by the message serializer.
 *
 * The <tt>charset</tt> applies to text messages, if the message is not in a text format, then no <tt>charset</tt>
 * should be specified.  This is not only used in setting of content negotiation headers, it's also used as a hint to
 * the framework of when it can treat a message as text.  For example, if the charset is set, then when a message gets
 * sent via WebSockets, it will be sent as a text message, otherwise it will be sent as a binary message.
 *
 * The <tt>version</tt> is used to describe the version of the protocol being used. Lagom does not, out of the box,
 * prescribe any semantics around the version, from Lagom's perspective, two message protocols with different versions
 * are two different protocols. The version is however treated as a separate piece of information so that generic
 * parsers, such as json/xml, can make sensible use of the content type passed to them.  The version could come from
 * a media type header, but it does not necessarily have to come from there, it could come from the URI or any other
 * header.
 *
 * <tt>MessageProtocol</tt> instances can also be used in content negotiation, an empty value means that any value
 * is accepted.
 */
final class MessageProtocol private[transport] (private[lagom] val delegate: CoreMessageProtocol) extends GenericMessageProtocol {

  /**
   * The content type of the protocol.
   *
   * @return The content type.
   */
  def contentType: Option[String] = delegate.contentType

  /**
   * The charset of the protocol.
   *
   * @return The charset.
   */
  def charset: Option[String] = delegate.charset

  /**
   * The version of the protocol.
   *
   * @return The version.
   */
  def version: Option[String] = delegate.version

  /**
   * Return a copy of this message protocol with the content type set to the given content type.
   *
   * @param contentType The content type to set.
   * @return A copy of this message protocol.
   */
  def withContentType(contentType: String): MessageProtocol =
    new MessageProtocol(delegate.withContentType(contentType))

  /**
   * Return a copy of this message protocol with the charset set to the given charset.
   *
   * @param charset The charset to set.
   * @return A copy of this message protocol.
   */
  def withCharset(charset: String): MessageProtocol =
    new MessageProtocol(delegate.withCharset(charset))

  /**
   * Return a copy of this message protocol with the version set to the given version.
   *
   * @param version The version to set.
   * @return A copy of this message protocol.
   */
  def withVersion(version: String): MessageProtocol =
    new MessageProtocol(delegate.withVersion(version))

  override def equals(that: Any): Boolean = {
    if (this == that) true
    else that match {
      case that: MessageProtocol => delegate == that.delegate
      case _                     => false
    }
  }

  override def hashCode: Int = delegate.hashCode

  override def toString: String = delegate.toString
}

object MessageProtocol {

  /**
   * Create a message protocol with the given content type, charset and version.
   *
   * @param contentType The content type.
   * @param charset The charset.
   * @param version The version.
   */
  def apply(contentType: Option[String] = Option.empty, charset: Option[String] = Option.empty, version: Option[String] = Option.empty) =
    new MessageProtocol(CoreMessageProtocol(contentType, charset, version))

  /**
   * Parse a message protocol from a content type header, if defined.
   *
   * @param contentType The content type header to parse.
   * @return The parsed message protocol.
   */
  def fromContentTypeHeader(contentType: Option[String]): MessageProtocol =
    new MessageProtocol(CoreMessageProtocol.fromContentTypeHeader(contentType))
}
