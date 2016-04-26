/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api.paging

/**
 * Captures paging information.
 */
case class CorePage(pageNo: Option[Int], pageSize: Option[Int]) {
  override def toString: String = s"Page{pageNo=$pageNo, pageSize=$pageSize}"
}
