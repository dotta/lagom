/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.javadsl.api.transport;

import java.util.regex.Pattern;

import com.lightbend.lagom.internal.api.transport.CorePathVersionedProtocolNegotiationStrategy;
import com.lightbend.lagom.internal.api.transport.InternalRequestHeader;
import com.lightbend.lagom.internal.api.transport.InternalResponseHeader;

/**
 * Negotiates the protocol using versions from the path.
 */
public class PathVersionedProtocolNegotiationStrategy implements HeaderTransformer {

  private final CorePathVersionedProtocolNegotiationStrategy delegate;

  private PathVersionedProtocolNegotiationStrategy(CorePathVersionedProtocolNegotiationStrategy delegate) {
    this.delegate = delegate;
  }

  public PathVersionedProtocolNegotiationStrategy(Pattern pathVersionExtractor, String pathVersionFormat) {
    this(new CorePathVersionedProtocolNegotiationStrategy(pathVersionExtractor,
        pathVersionFormat));
  }

  public PathVersionedProtocolNegotiationStrategy() {
    this(CorePathVersionedProtocolNegotiationStrategy.apply());
  }

  @Override
  public ResponseHeader transformServerResponse(ResponseHeader response, RequestHeader request) {
    return new InternalResponseHeader(delegate.transformServerResponse(InternalResponseHeader.obtainDelegate(response), InternalRequestHeader.obtainDelegate(request)));
  }

  @Override
  public ResponseHeader transformClientResponse(ResponseHeader response, RequestHeader request) {
    return new InternalResponseHeader(delegate.transformClientResponse(InternalResponseHeader.obtainDelegate(response), InternalRequestHeader.obtainDelegate(request)));
  }

  @Override
  public RequestHeader transformServerRequest(RequestHeader request) {
    return new InternalRequestHeader(delegate.transformServerRequest(InternalRequestHeader.obtainDelegate(request)));
  }

  @Override
  public RequestHeader transformClientRequest(RequestHeader request) {
    return new InternalRequestHeader(delegate.transformClientRequest(InternalRequestHeader.obtainDelegate(request)));
  }
}
