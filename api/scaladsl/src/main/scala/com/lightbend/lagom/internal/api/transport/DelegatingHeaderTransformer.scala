/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api.transport

import com.lightbend.lagom.scaladsl.api.transport.HeaderTransformer
import com.lightbend.lagom.scaladsl.api.transport.RequestHeader
import com.lightbend.lagom.scaladsl.api.transport.ResponseHeader

class DelegatingHeaderTransformer(delegate: CoreHeaderTransformer) extends HeaderTransformer {
  override def transformServerResponse(response: ResponseHeader, request: RequestHeader): ResponseHeader =
    new ResponseHeader(delegate.transformServerResponse(response.delegate, request.delegate))

  override def transformClientResponse(response: ResponseHeader, request: RequestHeader): ResponseHeader =
    new ResponseHeader(delegate.transformClientResponse(response.delegate, request.delegate))

  override def transformServerRequest(request: RequestHeader): RequestHeader =
    new RequestHeader(delegate.transformServerRequest(request.delegate))

  override def transformClientRequest(request: RequestHeader): RequestHeader =
    new RequestHeader(delegate.transformClientRequest(request.delegate))
}
