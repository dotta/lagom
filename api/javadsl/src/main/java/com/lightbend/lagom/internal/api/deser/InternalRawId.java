/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api.deser;

import java.util.Optional;

import org.pcollections.PMap;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import com.lightbend.lagom.internal.converter.Collection;
import com.lightbend.lagom.javadsl.api.deser.RawId;

import scala.compat.java8.OptionConverters;

public final class InternalRawId implements RawId {

  private final CoreRawId delegate;

  public InternalRawId(CoreRawId delegate) {
    this.delegate = delegate;
  }

  public static CoreRawId obtainDelegate(RawId rawId) {
    return ((InternalRawId) rawId).delegate;
  }

  public PSequence<RawId.PathParam> pathParams() {
    return Collection.map(delegate.pathParams(), RawId.PathParam::new);
  }

  public PMap<String, PSequence<String>> queryParams() {
    return Collection.asPCollection(delegate.queryParams());
  }

  public Optional<String> pathParam(String name) {
    return pathParams().stream().filter(p -> p.name().map(name::equals).orElse(false)).findFirst()
        .map(RawId.PathParam::value);
  }

  public Optional<String> queryParam(String name) {
    return Optional.ofNullable(queryParams().get(name)).flatMap(p -> {
      if (p.isEmpty()) {
        return Optional.empty();
      } else {
        return Optional.of(p.get(0));
      }
    });
  }

  public InternalRawId withQueryParam(String name, PSequence<String> values) {
    return new InternalRawId(delegate.withQueryParam(name, Collection.asScala(values)));
  }

  public InternalRawId withQueryParam(String name, Optional<String> value) {
    return new InternalRawId(delegate.withQueryParam(name, OptionConverters.toScala(value)));
  }

  public InternalRawId withQueryParam(String name, String value) {
    return withQueryParam(name, TreePVector.singleton(value));
  }

  public InternalRawId withPathParam(String name, String value) {
    return new InternalRawId(delegate.withPathParam(name, value));
  }

  public InternalRawId withPathValue(String value) {
    return new InternalRawId(delegate.withPathValue(value));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    RawId that = (RawId) o;

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
