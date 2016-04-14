/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.javadsl.api.security;

import com.lightbend.lagom.internal.api.security.CoreUserAgentServiceIdentificationStrategy;
import com.lightbend.lagom.internal.api.transport.InternalRequestHeader;
import com.lightbend.lagom.javadsl.api.transport.HeaderTransformer;
import com.lightbend.lagom.javadsl.api.transport.RequestHeader;
import com.lightbend.lagom.javadsl.api.transport.ResponseHeader;

/**
 * Transfers service principal information via the <tt>User-Agent</tt> header.
 *
 * If using this on a service that serves requests from the outside world, it
 * would be a good idea to block the <tt>User-Agent</tt> header in the web
 * facing load balancer/proxy.
 */
public class UserAgentServiceIdentificationStrategy implements HeaderTransformer {

  private final CoreUserAgentServiceIdentificationStrategy delegate;

  private UserAgentServiceIdentificationStrategy(CoreUserAgentServiceIdentificationStrategy delegate) {
    this.delegate = delegate;
  }

  public UserAgentServiceIdentificationStrategy() {
    this(new CoreUserAgentServiceIdentificationStrategy());
  }

  @Override
  public RequestHeader transformClientRequest(RequestHeader request) {
    return new InternalRequestHeader(delegate.transformClientRequest(InternalRequestHeader.obtainDelegate(request)));
  }

  @Override
  public RequestHeader transformServerRequest(RequestHeader request) {
    return new InternalRequestHeader(delegate.transformServerRequest(InternalRequestHeader.obtainDelegate(request)));
  }

  @Override
  public ResponseHeader transformServerResponse(ResponseHeader response, RequestHeader request) {
    return response;
  }

  @Override
  public ResponseHeader transformClientResponse(ResponseHeader response, RequestHeader request) {
    return response;
  }
}
