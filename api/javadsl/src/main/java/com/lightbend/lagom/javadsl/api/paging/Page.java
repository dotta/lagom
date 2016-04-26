/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.javadsl.api.paging;

import java.util.OptionalInt;

import scala.compat.java8.OptionConverters;

import com.lightbend.lagom.internal.api.paging.CorePage;

/**
 * A page object, use to capture paging information.
 */
public final class Page {
  private final CorePage delegate;

  private Page(CorePage delegate) {
    this.delegate = delegate;
  }

  public Page(OptionalInt pageNo, OptionalInt pageSize) {
    this(CorePage.apply(OptionConverters.toScala(pageNo), OptionConverters.toScala(pageSize)));
  }

  public OptionalInt pageNo() {
    return this.delegate.pageNo().asPrimitive();
  }

  public OptionalInt pageSize() {
    return this.delegate.pageSize().asPrimitive();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Page page = (Page) that;

    return delegate.equals(that.delegate);

  }

  @Override
  public int hashCode() {
    return delegate.hashCode();
  }

  @Override
  public String toString() {
    return delegate.toString();
  }
}
