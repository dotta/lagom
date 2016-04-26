/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api

import com.lightbend.lagom.api.GenericService
import com.lightbend.lagom.internal.api.deser.CoreExceptionSerializer
import com.lightbend.lagom.internal.api.deser.CoreSerializerFactory

trait ServiceReader {
  def readServiceDescriptor(classLoader: ClassLoader, serviceInterface: Class[_ <: GenericService]): CoreDescriptor

  def resolveServiceDescriptor(descriptor: CoreDescriptor, classLoader: ClassLoader,
                               builtInSerializerFactories:  Map[PlaceholderSerializerFactory, CoreSerializerFactory],
                               builtInExceptionSerializers: Map[PlaceholderExceptionSerializer, CoreExceptionSerializer]): CoreDescriptor
}
