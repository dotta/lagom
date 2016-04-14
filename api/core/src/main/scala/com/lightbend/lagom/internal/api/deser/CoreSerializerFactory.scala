/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api.deser

import java.lang.reflect.Type;

/**
 * A serializer factory is responsible for constructing serializers for types.
 *
 * It is used when no serializer is explicitly defined for a message type, either specific to the endpoint, or for
 * a descriptor.
 */
trait CoreSerializerFactory {

  /**
   * Get a message serializer for the given type.
   *
   * @param tpe The type to get a message serializer for.
   * @return The message serializer.
   */
  def messageSerializerFor[MessageEntity](tpe: Type): CoreMessageSerializer[MessageEntity, _]

}

object CoreSerializerFactory {
  object PlaceholderSerializerFactory extends CoreSerializerFactory {
    override def messageSerializerFor[MessageEntity](tpe: Type): CoreMessageSerializer[MessageEntity, _] =
      throw new NotImplementedError("This serializer factory is only a placeholder, and cannot be used directly")
  }
}