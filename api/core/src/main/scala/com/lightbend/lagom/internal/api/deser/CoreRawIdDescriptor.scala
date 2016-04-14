/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api.deser

import scala.collection.immutable.Seq

case class CoreRawIdDescriptor(pathParams: Seq[String], queryParams: Seq[String]) {

  def tail: CoreRawIdDescriptor = {
    if (pathParams.nonEmpty) copy(pathParams = pathParams.tail)
    else if (queryParams.nonEmpty) copy(queryParams = queryParams.tail)
    else throw new IllegalArgumentException("No parameters to remove from descriptor")
  }

  override def toString: String =
    s"RawIdDescriptor{pathParams=$pathParams, queryParams=$queryParams}"

}
