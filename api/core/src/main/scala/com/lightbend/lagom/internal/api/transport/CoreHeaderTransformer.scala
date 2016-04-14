/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api.transport

trait CoreHeaderTransformer {
  /**
   * Transform the given client request.
   *
   * This will be invoked on all requests outgoing from the client.
   *
   * @param request The client request header.
   * @return The transformed client request header.
   */
  def transformClientRequest(request: CoreRequestHeader): CoreRequestHeader

  /**
   * Transform the given server request.
   *
   * This will be invoked on all requests incoming to the server.
   *
   * @param request The server request header.
   * @return The transformed server request header.
   */
  def transformServerRequest(request: CoreRequestHeader): CoreRequestHeader

  /**
   * Transform the given server response.
   *
   * This will be invoked on all responses outgoing from the server.
   *
   * @param response The server response.
   * @param request The transformed server request. Useful for when the response transformation requires information
   *                from the client.
   * @return The transformed server response header.
   */
  def transformServerResponse(response: CoreResponseHeader, request: CoreRequestHeader): CoreResponseHeader

  /**
   * Transform the given client response.
   *
   * This will be invoked on all responses incoming to the client.
   *
   * @param response The client response.
   * @param request The client request. Useful for when the response transformation requires information from the
   *                client request.
   * @return The transformed client response header.
   */
  def transformClientResponse(response: CoreResponseHeader, request: CoreRequestHeader): CoreResponseHeader
}

object CoreHeaderTransformer {
  /**
   * A noop header transformer, used to deconfigure specific transformers.
   */
  case object NullHeaderTransformer extends CoreHeaderTransformer {
    override def transformClientRequest(request: CoreRequestHeader): CoreRequestHeader = request

    override def transformServerRequest(request: CoreRequestHeader): CoreRequestHeader = request

    override def transformServerResponse(response: CoreResponseHeader, request: CoreRequestHeader): CoreResponseHeader = response

    override def transformClientResponse(response: CoreResponseHeader, request: CoreRequestHeader): CoreResponseHeader = response
  }
}
