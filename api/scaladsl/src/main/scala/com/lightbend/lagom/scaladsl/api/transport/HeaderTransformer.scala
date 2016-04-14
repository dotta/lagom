/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.scaladsl.api.transport

/**
 * Transformer of headers.
 *
 * This is used to transform transport and protocol headers according to various
 * strategies for protocol and version negotiation, as well as authentication.
 */
trait HeaderTransformer {

  /**
   * Transform the given client request.
   *
   * This will be invoked on all requests outgoing from the client.
   *
   * @param request
   *          The client request header.
   * @return The transformed client request header.
   */
  def transformClientRequest(request: RequestHeader): RequestHeader

  /**
   * Transform the given server request.
   *
   * This will be invoked on all requests incoming to the server.
   *
   * @param request
   *          The server request header.
   * @return The transformed server request header.
   */
  def transformServerRequest(request: RequestHeader): RequestHeader

  /**
   * Transform the given server response.
   *
   * This will be invoked on all responses outgoing from the server.
   *
   * @param response
   *          The server response.
   * @param request
   *          The transformed server request. Useful for when the response
   *          transformation requires information from the client.
   * @return The transformed server response header.
   */
  def transformServerResponse(response: ResponseHeader, request: RequestHeader): ResponseHeader

  /**
   * Transform the given client response.
   *
   * This will be invoked on all responses incoming to the client.
   *
   * @param response
   *          The client response.
   * @param request
   *          The client request. Useful for when the response transformation
   *          requires information from the client request.
   * @return The transformed client response header.
   */
  def transformClientResponse(response: ResponseHeader, request: RequestHeader): ResponseHeader
}

object HeaderTransformer {
  /**
   * A noop header transformer, used to deconfigure specific transformers.
   */
  case object NullHeaderTransformer extends HeaderTransformer {
    override def transformClientRequest(request: RequestHeader): RequestHeader = request

    override def transformServerRequest(request: RequestHeader): RequestHeader = request

    override def transformServerResponse(response: ResponseHeader, request: RequestHeader): ResponseHeader = response

    override def transformClientResponse(response: ResponseHeader, request: RequestHeader): ResponseHeader = response
  }
}
