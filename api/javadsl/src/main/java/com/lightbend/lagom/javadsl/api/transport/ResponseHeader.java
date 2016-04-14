/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.javadsl.api.transport;

import org.pcollections.PMap;
import org.pcollections.PSequence;

import com.lightbend.lagom.internal.api.transport.CoreResponseHeader;
import com.lightbend.lagom.internal.api.transport.InternalResponseHeader;

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
public abstract class ResponseHeader extends MessageHeader {
  public static final ResponseHeader OK = new InternalResponseHeader(CoreResponseHeader.Ok());
  public static final ResponseHeader NO_CONTENT = new InternalResponseHeader(CoreResponseHeader.NoContent());

  protected final CoreResponseHeader delegate;

  protected ResponseHeader(CoreResponseHeader delegate) {
    super(delegate);
    this.delegate = delegate;
  }

  protected ResponseHeader(int status, MessageProtocol protocol, PMap<String, PSequence<String>> headers) {
    this(CoreResponseHeader.of(status, protocol.delegate, headers));
  }

  /**
   * Get the status of this response.
   *
   * @return The status of this response.
   */
  public int status() {
    return delegate.status();
  }

  /**
   * Return a copy of this response header with the status set.
   *
   * @param status
   *          The status to set.
   * @return A copy of this response header.
   */
  public ResponseHeader withStatus(int status) {
    return new InternalResponseHeader(delegate.withStatus(status));
  }

  @Override
  public ResponseHeader withProtocol(MessageProtocol protocol) {
    return new InternalResponseHeader(delegate.withProtocol(protocol.delegate));
  }

  @Override
  public ResponseHeader replaceAllHeaders(PMap<String, PSequence<String>> headers) {
    return new InternalResponseHeader(delegate.replaceAllHeaders(headers));
  }

  @Override
  public ResponseHeader withHeader(String name, String value) {
    return new InternalResponseHeader(delegate.withHeader(name, value));
  }

  @Override
  public String toString() {
    return delegate.toString();
  }

  @Override
  public int hashCode() {
    return delegate.hashCode();
  }
}
