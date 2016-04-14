/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api.deser

trait CoreIdSerializer[Id] {
  def serialize(id: Id): CoreRawId
  def deserialize(rawId: CoreRawId): Id
  def numPathParamsHint: Option[Int]
}
