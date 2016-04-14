/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.scaladsl.api.deser

import scala.collection.immutable.Seq
import com.lightbend.lagom.internal.api.deser.CoreRawIdDescriptor

final class RawIdDescriptor private (private val delegate: CoreRawIdDescriptor) {
  def pathParams: Seq[String] = delegate.pathParams
  def queryParams: Seq[String] = delegate.queryParams
  def removeNext: RawIdDescriptor = new RawIdDescriptor(delegate.tail)
  override def equals(that: Any): Boolean = {
    if (this == that) true
    else that match {
      case that: RawIdDescriptor => delegate == that.delegate
      case _                     => false
    }
  }

  override def hashCode: Int = delegate.hashCode
  override def toString: String = delegate.toString
}

object RawIdDescriptor {
  def apply(pathParams: Seq[String], queryParams: Seq[String]): RawIdDescriptor = new RawIdDescriptor(CoreRawIdDescriptor(pathParams, queryParams))
}
