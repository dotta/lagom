/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.javadsl.api.transport;

import java.util.Optional;

import com.lightbend.lagom.internal.api.transport.CoreMessageHeader;
import com.lightbend.lagom.internal.converter.Collection;

import org.pcollections.PMap;
import org.pcollections.PSequence;

import scala.compat.java8.OptionConverters;


/**
 * A message header.
 * 
 * Note: This class is not meant to be extended or instantiated by clients.
 */
public abstract class MessageHeader {

  private final CoreMessageHeader delegate;

  protected MessageHeader(CoreMessageHeader delegate) {
    this.delegate = delegate;
  }

  /**
   * Get the protocol of the message.
   *
   * @return The protocol.
   */
  public final MessageProtocol protocol() {
    return new MessageProtocol(delegate.protocol());
  }

  /**
   * Get the headers for the message.
   *
   * The returned map is case sensitive, it is recommended that you use
   * <tt>getHeader</tt> instead.
   *
   * @return The headers for this message.
   */
  public final PMap<String, PSequence<String>> headers() {
    return Collection.asPCollection(delegate.headers());
  }

  /**
   * Get the header with the given name.
   *
   * The lookup is case insensitive.
   *
   * @param name
   *          The name of the header.
   * @return The header value.
   */
  public final Optional<String> getHeader(String name) {
    return OptionConverters.toJava(delegate.header(name));
  }

  /**
   * Return a copy of this message header with the given protocol.
   *
   * @param protocol
   *          The protocol to set.
   * @return A copy of the message header with the given protocol.
   */
  abstract MessageHeader withProtocol(MessageProtocol protocol);

  /**
   * Return a copy of this message header with the headers replaced by the given
   * map of headers.
   *
   * @param headers
   *          The map of headers.
   * @return A copy of the message header with the given headers.
   */
  abstract MessageHeader replaceAllHeaders(PMap<String, PSequence<String>> headers);

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
  abstract MessageHeader withHeader(String name, String value);

}
