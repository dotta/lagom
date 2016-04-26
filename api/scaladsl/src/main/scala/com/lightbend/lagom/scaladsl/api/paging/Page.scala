/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.scaladsl.api.paging

import com.lightbend.lagom.internal.api.paging.CorePage

/**
 * Captures paging information.
 */
final class Page private (private val delegate: CorePage) {
  def pageNo: Option[Int] = delegate.pageNo
  def pageSize: Option[Int] = delegate.pageSize

  override def equals(that: Any): Boolean = {
    if (this == that) true
    else that match {
      case that: Page => delegate == that.delegate
      case _          => false
    }
  }

  override def hashCode: Int = delegate.hashCode
  override def toString: String = delegate.toString
}

object Page {
  /**
   * Factory method for creating `Page` instances.
   */
  def apply(pageNo: Option[Int], pageSize: Option[Int]): Page = new Page(CorePage(pageNo, pageSize))
}
