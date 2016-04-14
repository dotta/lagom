/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api.transport;

import org.pcollections.PMap;
import org.pcollections.PSequence;

import com.lightbend.lagom.javadsl.api.transport.MessageProtocol;
import com.lightbend.lagom.javadsl.api.transport.ResponseHeader;

/**
 * Internal implementation of RequestHeader for the Java API, allowing to obtain
 * a reference on the {@link CoreRequestHeader} instance, and hence permitting
 * to bridge the Java API and core implementations.
 * 
 * All it offers is an extra method {@link InternalResponseHeader#internal} for
 * retrieving the {@link CoreRequestHeader}.
 * 
 * Note that this is the only concrete implementation of the
 * {@link ResponseHeader} class for the Java API, and that's why the equality
 * contract is implemented here in terms of {@link InternalResponseHeader}. No
 * other concrete kind of instances are expected for {@link ResponseHeader}.
 * 
 * This class is not expected to be instantiated by users.
 */
public final class InternalResponseHeader extends ResponseHeader {

  public InternalResponseHeader(CoreResponseHeader delegate) {
    super(delegate);
  }

  public InternalResponseHeader(int status, MessageProtocol protocol, PMap<String, PSequence<String>> headers) {
    super(status, protocol, headers);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof InternalResponseHeader))
      return false;

    InternalResponseHeader that = (InternalResponseHeader) o;
    return delegate.equals(that.delegate);
  }

  public static CoreResponseHeader obtainDelegate(ResponseHeader request) {
    return ((InternalResponseHeader) request).delegate;
  }
}
