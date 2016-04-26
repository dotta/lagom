/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api

import java.util.function.{ BiFunction => JFunction2 }
import scala.compat.java8.FunctionConverters._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import com.lightbend.lagom.internal.api.transport.CoreRequestHeader
import com.lightbend.lagom.internal.api.transport.CoreResponseHeader

import akka.NotUsed
import java.util.concurrent.CompletionStage

/**
 * A service call for an entity with a particular id.
 *
 * The entity id corresponds to dynamic parts of a path. If {@link NotUsed}, that implies
 * it will be for a static path.
 *
 * A service call has a request and a response entity. Either entity may be NotUsed, if there is no entity associated
 * with the call. They may also be an Akka streams Source, in situations where the endpoint serves a stream. In all
 * other cases, the entities will be considered "strict" entities, that is, they will be parsed into memory, eg,
 * using json.
 */
class CoreServiceCall[Id, Request, Response](f: (Id, Request) => Future[Response]) {
  def apply(id: Id, request: Request): Future[Response] = f(id, request)
}

object CoreServiceCall {
  def apply[Id, Request, Response](f: JFunction2[Id, Request, CompletionStage[Response]]): CoreServiceCall[Id, Request, Response] =
    new CoreServiceCall(f.asScala.)
}
//{ self =>
//
//  /**
//   * Invoke the service call.
//   *
//   * @param id The id of the entity.
//   * @param request The request entity.
//   * @return A future of the response entity.
//   */
//  def apply(id: Id, request: Request): Future[Response]
//
//  /**
//   * Invoke the service call with a unit id argument.
//   *
//   * This should only be used when the id is unit.
//   *
//   * @param request The request entity.
//   * @return A future of the response entity.
//   */
//  def apply(request: Request): Future[Response] =
//    apply(NotUsed.asInstanceOf[Id], request)
//
//  /**
//   * Invoke the service call with unit id argument and a unit request message.
//   *
//   * This should only be used when the id and the request message are both unit.
//   *
//   * @return A future of the response entity.
//   */
//  def apply(): Future[Response] =
//    apply(NotUsed.asInstanceOf[Id], NotUsed.asInstanceOf[Request])
//
//  /**
//   * Make any modifications necessary to the request header.
//   *
//   * For client service calls, this gives clients an opportunity to add custom headers and/or modify the request in
//   * some way before it is made.  The passed in handler is applied before the header transformers
//   * configured for the descriptor/endpoint are applied.
//   *
//   * For server implementations of service calls, this will be invoked by the server in order to supply the request
//   * header.  A new service call can then be returned that uses the header.  The header passed in to the handler by
//   * the service call can be anything, it will be ignored - {@link RequestHeader#DEFAULT} exists for this
//   * purpose.  Generally, server implementations should not implement this method directly, rather, they should use
//   * <tt>ServerServiceCall</tt>, which provides an appropriate implementation.
//   *
//   * @param handler A function that takes in the request header representing the request, and transforms it.
//   * @return A service call that will use the given handler.
//   */
//  def handleRequestHeader(handler: CoreRequestHeader => CoreRequestHeader): CoreServiceCall[Id, Request, Response] = {
//    // Default implementation. For client service calls, this is overridden by the implementation to do something
//    // with the handler.
//    this
//  }
//
//  /**
//   * Transform the response using the given function that takes the response header and the response.
//   *
//   * For client service calls, this gives clients an opportunity to inspect the response headers and status code.
//   * The passed in handler is applied after the header transformers configured for the descriptor/endpoint are
//   * applied.
//   *
//   * For server implementations of service calls, this will be invoked by the server in order to give the service
//   * call an opportunity to supply the response header when it supplies the response, but only if the underlying
//   * transport supports sending a response header.  Generally, server implementations should not implement this
//   * method directly, rather, they should use <tt>ServerServiceCall</tt>, which provides an appropriate
//   * implementation.
//   *
//   * @param handler The handler.
//   * @return A service call that uses the given handler.
//   */
//  def handleResponseHeader[T](handler: (CoreResponseHeader, Response) => T)(implicit ec: ExecutionContext): CoreServiceCall[Id, Request, T] = new CoreServiceCall[Id, Request, T] {
//    // Default implementation. For client service calls, this is overridden by the implementation to do something
//    // with the handler.
//    def apply(id: Id, request: Request) =
//      self(id, request).map(response => handler(CoreResponseHeader.Ok, response))
//  }
//
//  /**
//   * Allow handling of the response header.
//   *
//   * This converts the service call to one that returns both the response header and the response message.
//   *
//   * This is simply a convenience method for invoking <code>handleResponseHeader(Pair::create)</code>.
//   *
//   * @return The a service call that returns the response header and the response message.
//   */
//  def withResponseHeader(implicit ec: ExecutionContext): CoreServiceCall[Id, Request, (CoreResponseHeader, Response)] = {
//    handleResponseHeader(Tuple2.apply)
//  }
//}
