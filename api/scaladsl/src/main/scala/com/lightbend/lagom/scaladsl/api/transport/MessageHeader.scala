/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.scaladsl.api.transport

import com.lightbend.lagom.internal.api.transport.CoreMessageHeader
import scala.collection.immutable.Seq

/**
 * A message header.
 */
abstract class MessageHeader protected (delegate: CoreMessageHeader) {

  /**
   * Get the protocol of the message.
   *
   * @return The protocol.
   */
  final def protocol: MessageProtocol = new MessageProtocol(delegate.protocol)

  /**
   * Get the headers for the message.
   *
   * The returned map is case sensitive, it is recommended that you use
   * <tt>getHeader</tt> instead.
   *
   * @return The headers for this message.
   */
  final def headers: Map[String, Seq[String]] = delegate.headers

  /**
   * Get the header with the given name.
   *
   * The lookup is case insensitive.
   *
   * @param name
   *          The name of the header.
   * @return The header value.
   */
  final def header(name: String): Option[String] = delegate.header(name)

  /**
   * Return a copy of this message header with the given protocol.
   *
   * @param protocol
   *          The protocol to set.
   * @return A copy of the message header with the given protocol.
   */
  def withProtocol(protocol: MessageProtocol): MessageHeader

  /**
   * Return a copy of this message header with the headers replaced by the given
   * map of headers.
   *
   * @param headers
   *          The map of headers.
   * @return A copy of the message header with the given headers.
   */
  def replaceAllHeaders(headers: Map[String, Seq[String]]): MessageHeader

  /**
   * Return a copy of this message header with the given header added to the map
   * of headers.
   *
   * If the header already has a value, this value will replace it.
   *
   * @param name
   *          The name of the header to add.
   * @param value
   *          The value of the header to add.
   * @return The new message header.
   */
  def withHeader(name: String, value: String): MessageHeader

  final override def toString: String = delegate.toString
  override def hashCode: Int = delegate.hashCode
}
