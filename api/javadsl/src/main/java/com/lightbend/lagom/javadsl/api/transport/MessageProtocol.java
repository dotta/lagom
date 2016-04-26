/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.javadsl.api.transport;

import java.util.Optional;

import com.lightbend.lagom.api.transport.GenericMessageProtocol;
import com.lightbend.lagom.internal.api.transport.CoreMessageProtocol;
import com.lightbend.lagom.internal.api.transport.InternalMessageProtocol;

/**
 * A message protocol.
 *
 * This describes the negotiated protocol being used for a message. It has three
 * elements, a content type, a charset, and a version.
 *
 * The <tt>contentType</tt> may be registered mime type such as
 * <tt>application/json</tt>, or it could be an application specific content
 * type, such as <tt>application/vnd.myservice+json</tt>. It could also contain
 * protocol versioning information, such as
 * <tt>application/vnd.github.v3+json</tt>. During the protocol negotiation
 * process, the content type may be transformed, for example, if the content
 * type contains a version, the configured {@link HeaderTransformer} will be
 * expected to extract that version out into the <tt>version</tt>, leaving a
 * <tt>contentType</tt> that will be understood by the message serializer.
 *
 * The <tt>charset</tt> applies to text messages, if the message is not in a
 * text format, then no <tt>charset</tt> should be specified. This is not only
 * used in setting of content negotiation headers, it's also used as a hint to
 * the framework of when it can treat a message as text. For example, if the
 * charset is set, then when a message gets sent via WebSockets, it will be sent
 * as a text message, otherwise it will be sent as a binary message.
 *
 * The <tt>version</tt> is used to describe the version of the protocol being
 * used. Lagom does not, out of the box, prescribe any semantics around the
 * version, from Lagom's perspective, two message protocols with different
 * versions are two different protocols. The version is however treated as a
 * separate piece of information so that generic parsers, such as json/xml, can
 * make sensible use of the content type passed to them. The version could come
 * from a media type header, but it does not necessarily have to come from
 * there, it could come from the URI or any other header.
 *
 * <tt>MessageProtocol</tt> instances can also be used in content negotiation,
 * an empty value means that any value is accepted.
 */
public interface MessageProtocol implements GenericMessageProtocol {

  /**
   * Create a message protocol with the given content type, charset and version.
   *
   * @param contentType
   *          The content type.
   * @param charset
   *          The charset.
   * @param version
   *          The version.
   */
  public static MessageProtocol of(Optional<String> contentType, Optional<String> charset, Optional<String> version) {
    new InternalMessageProtocol(CoreMessageProtocol.of(contentType, charset, version));
  }

  /**
   * Create a message protocol that doesn't specify any content type, charset or
   * version.
   */
  public static MessageProtocol of() {
    of(Optional.empty(), Optional.empty(), Optional.empty());
  }

  /**
   * Parse a message protocol from a content type header, if defined.
   *
   * @param contentType
   *          The content type header to parse.
   * @return The parsed message protocol.
   */
  public static MessageProtocol fromContentTypeHeader(Optional<String> contentType) {
    CoreMessageProtocol delegate = CoreMessageProtocol
        .fromContentTypeHeader(OptionConverters.toScala(contentType));
    return new InternalMessageProtocol(delegate);
  }

  /**
   * The content type of the protocol.
   *
   * @return The content type.
   */
  Optional<String> contentType();

  /**
   * The charset of the protocol.
   *
   * @return The charset.
   */
  Optional<String> charset();

  /**
   * The version of the protocol.
   *
   * @return The version.
   */
  Optional<String> version();

  /**
   * Return a copy of this message protocol with the content type set to the
   * given content type.
   *
   * @param contentType
   *          The content type to set.
   * @return A copy of this message protocol.
   */
  MessageProtocol withContentType(String contentType);

  /**
   * Return a copy of this message protocol with the charset set to the given
   * charset.
   *
   * @param charset
   *          The charset to set.
   * @return A copy of this message protocol.
   */
  MessageProtocol withCharset(String charset);

  /**
   * Return a copy of this message protocol with the version set to the given
   * version.
   *
   * @param version
   *          The version to set.
   * @return A copy of this message protocol.
   */
  MessageProtocol withVersion(String version);

  /**
   * Whether this message protocol is a text based protocol.
   *
   * This is determined by whether the charset is defined.
   *
   * @return true if this message protocol is text based.
   */
  boolean isText();

  /**
   * Whether the protocol uses UTF-8.
   *
   * @return true if the charset used by this protocol is UTF-8, false if it's
   *         some other encoding or if no charset is defined.
   */
  boolean isUtf8();

  /**
   * Convert this message protocol to a content type header, if the content type
   * is defined.
   *
   * @return The message protocol as a content type header.
   */
  Optional<String> toContentTypeHeader();
}
