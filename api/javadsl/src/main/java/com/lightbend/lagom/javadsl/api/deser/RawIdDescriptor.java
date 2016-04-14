/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.javadsl.api.deser;

import org.pcollections.PSequence;

import com.lightbend.lagom.internal.api.deser.CoreRawIdDescriptor;
import com.lightbend.lagom.internal.converter.Collection;


public final class RawIdDescriptor {
    private final CoreRawIdDescriptor delegate;

    private RawIdDescriptor(CoreRawIdDescriptor delegate) {
      this.delegate = delegate;
    }
    public RawIdDescriptor(PSequence<String> pathParams, PSequence<String> queryParams) {
        this(CoreRawIdDescriptor.apply(Collection.asScala(pathParams), Collection.asScala(queryParams)));
    }

    public PSequence<String> pathParams() {
        return Collection.asPCollection(delegate.pathParams());
    }

    public PSequence<String> queryParams() {
      return Collection.asPCollection(delegate.queryParams());
    }

    public RawIdDescriptor removeNext() {
      return new RawIdDescriptor(delegate.tail());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RawIdDescriptor that = (RawIdDescriptor) o;

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
