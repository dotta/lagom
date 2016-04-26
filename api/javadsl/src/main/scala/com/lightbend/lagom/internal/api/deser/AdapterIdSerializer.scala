/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api.deser

import com.lightbend.lagom.javadsl.api.deser.IdSerializer
import scala.compat.java8.OptionConverters._

/**
 * Exposes a Java DSL IdSerializer type as a CoreIdSerializer.
 */
class AdapterIdSerializer[Id](val idSerializer: IdSerializer[Id]) extends CoreIdSerializer[Id] {
  def serialize(id: Id): CoreRawId = {
    val rawId = idSerializer.serialize(id)
    InternalRawId.obtainDelegate(rawId)
  }
  def deserialize(rawId: CoreRawId): Id = idSerializer.deserialize(new InternalRawId(rawId))
  def numPathParamsHint: Option[Int] = idSerializer.numPathParamsHint().asScala
}
