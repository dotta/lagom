/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.scaladsl.api.paging

/**
 * Captures paging information.
 */
sealed trait Page {
  def pageNo: Option[Int]
  def pageSize: Option[Int]
}

object Page {
  /**
   * Factory method for creating `Page` instances.
   */
  def apply(pageNo: Option[Int], pageSize: Option[Int]): Page = PageImpl(pageNo, pageSize)

  private[this] case class PageImpl(pageNo: Option[Int], pageSize: Option[Int]) extends Page
}
