/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.javadsl.api;

import java.util.Optional;

import com.lightbend.lagom.api.transport.Method;
import com.lightbend.lagom.internal.api.CoreServiceAcl;

import scala.compat.java8.OptionConverters;

public final class ServiceAcl {

    public static ServiceAcl path(String pathRegex) {
        return new ServiceAcl(Optional.empty(), Optional.of(pathRegex));
    }

    public static ServiceAcl methodAndPath(Method method, String pathRegex) {
        return new ServiceAcl(Optional.of(method), Optional.of(pathRegex));
    }
    
    private final CoreServiceAcl delegate;

    public ServiceAcl(Optional<Method> method, Optional<String> pathRegex) {
        this.delegate = new CoreServiceAcl(OptionConverters.toScala(method), OptionConverters.toScala(pathRegex));
    }

    public Optional<Method> method() {
        return OptionConverters.toJava(delegate.method());
    }

    public Optional<String> pathRegex() {
        return OptionConverters.toJava(delegate.pathRegex());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceAcl)) return false;

        ServiceAcl that = (ServiceAcl) o;
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
