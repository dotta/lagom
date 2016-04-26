/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api.transport;


import java.util.Optional;

import com.lightbend.lagom.javadsl.api.transport.MessageProtocol;

import scala.compat.java8.OptionConverters;

/**
 * 
 */
public final class InternalMessageProtocol implements MessageProtocol {

  private final CoreMessageProtocol delegate;

  public static CoreMessageProtocol obtainDelegate(MessageProtocol protocol) {
    return ((InternalMessageProtocol) protocol).delegate;
  }

  public InternalMessageProtocol(CoreMessageProtocol delegate) {
    this.delegate = delegate;
  }

  @Override
  public Optional<String> contentType() {
    return OptionConverters.toJava(delegate.contentType());
  }

  @Override
  public Optional<String> charset() {
    return OptionConverters.toJava(delegate.charset());
  }

  @Override
  public Optional<String> version() {
    return OptionConverters.toJava(delegate.version());
  }

  @Override
  public InternalMessageProtocol withContentType(String contentType) {
    return new InternalMessageProtocol(delegate.withContentType(contentType));
  }

  @Override
  public InternalMessageProtocol withCharset(String charset) {
    return new InternalMessageProtocol(delegate.withCharset(charset));
  }

  @Override
  public InternalMessageProtocol withVersion(String version) {
    return new InternalMessageProtocol(delegate.withVersion(version));
  }

  @Override
  public boolean isText() {
    return delegate.isText();
  }

  @Override
  public boolean isUtf8() {
    return delegate.isUtf8();
  }

  @Override
  public Optional<String> toContentTypeHeader() {
    return OptionConverters.toJava(delegate.contentTypeHeader());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof MessageProtocol))
      return false;

    MessageProtocol that = (MessageProtocol) o;
    return delegate.equals(that.delegate);
  }

  @Override
  public int hashCode() {
    return delegate.hashCode();
  }

  @Override
  public String toString() {
    return delegate.toString();
  }
}
