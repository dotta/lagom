/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.javadsl.api.deser;

import akka.util.ByteString;

import com.lightbend.lagom.javadsl.api.transport.MessageProtocol;
import com.lightbend.lagom.api.transport.TransportErrorCode;
import com.lightbend.lagom.internal.api.deser.CoreRawExceptionMessage;
import com.lightbend.lagom.internal.api.transport.CoreMessageProtocol;

import scala.compat.java8.OptionConverters;

/**
 * A serialized exception message.
 */
public class RawExceptionMessage {

    private final CoreRawExceptionMessage delegate;

    public RawExceptionMessage(TransportErrorCode errorCode, MessageProtocol protocol, ByteString message) {
      CoreMessageProtocol underlying = new CoreMessageProtocol(
          OptionConverters.toScala(protocol.contentType()),
          OptionConverters.toScala(protocol.charset()),
          OptionConverters.toScala(protocol.version()));
      this.delegate = new CoreRawExceptionMessage(errorCode, underlying, message);
    }

    public TransportErrorCode errorCode() {
        return delegate.errorCode();
    }

    public MessageProtocol protocol() {
      CoreMessageProtocol underlying = delegate.protocol();
      return new MessageProtocol(
          OptionConverters.toJava(underlying.contentType()),
          OptionConverters.toJava(underlying.charset()),
          OptionConverters.toJava(underlying.version()));
    }

    public ByteString message() {
        return delegate.message();
    }

    /**
     * Get the message as text.
     *
     * If this is a binary message (that is, the message protocol does not define a charset), encodes it using Base64.
     *
     * @return The message as text.
     */
    public String messageAsText() {
        return delegate.messageAsText();
    }
}
