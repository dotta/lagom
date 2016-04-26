/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api

import java.lang.reflect.Type

abstract class CoreService {
  /**
   * Describe this service.
   *
   * The intended mechanism for implementing this is to provide it as a default implementation on an interface.
   */
  def descriptor: CoreDescriptor
}

object CoreService {
  /**
   * A self describing service call.
   *
   * Self describing service calls return generic type information that is otherwise lost due to type erasure. They
   * are typically implemented by the Lagom framework, which inspects the return types of service calls.
   */
  trait SelfDescribingServiceCall[Id, Request, Response] extends CoreServiceCall[Id, Request, Response] {
    /**
     * Get the type of the ID.
     *
     * @return The type of the ID.
     */
    def idType: Type

    /**
     * Get the type of the request.
     *
     * @return The type of the request.
     */
    def requestType: Type

    /**
     * Get the type of the response.
     *
     * @return The type of the response.
     */
    def responseType: Type

    /**
     * Get the name of the method that defines the call.
     *
     * @return The name of the method.
     */
    def methodName: String
  }

}