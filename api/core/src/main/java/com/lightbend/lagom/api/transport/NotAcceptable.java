/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.api.transport;

import java.util.Collection;
import java.util.stream.Collectors;

import com.lightbend.lagom.api.deser.ExceptionMessage;

import scala.collection.immutable.Seq;
import scala.collection.JavaConversions;

/**
 * Thrown when a protocol requested by the client cannot be negotiated.
 */
public class NotAcceptable extends TransportException {
  public static final TransportErrorCode ERROR_CODE = TransportErrorCode.NotAcceptable;

  public NotAcceptable(Collection<? extends GenericMessageProtocol> requested, GenericMessageProtocol supported) {
    super(ERROR_CODE,
        "The requested protocol type/versions: ("
            + requested.stream().map(GenericMessageProtocol::toString).collect(Collectors.joining(", "))
            + ") could not be satisfied by the server, the default that the server uses is: " + supported);
  }

  public NotAcceptable(Seq<GenericMessageProtocol> requested, GenericMessageProtocol supported) {
    this(JavaConversions.asJavaCollection(requested), supported);
  }

  public NotAcceptable(TransportErrorCode errorCode, ExceptionMessage exceptionMessage) {
    super(errorCode, exceptionMessage);
  }
}
