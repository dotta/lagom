/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api.transport;

import java.net.URI;
import java.security.Principal;
import java.util.Optional;

import org.pcollections.PMap;
import org.pcollections.PSequence;

import com.lightbend.lagom.api.transport.Method;
import com.lightbend.lagom.javadsl.api.transport.MessageProtocol;
import com.lightbend.lagom.javadsl.api.transport.RequestHeader;

/**
 * Internal implementation of RequestHeader for the Java API, allowing to obtain
 * a reference on the {@link CoreRequestHeader} instance, and hence permitting
 * to bridge the Java API and core implementations.
 * 
 * All it offers is an extra method {@link InternalRequestHeader#internal} for
 * retrieving the {@link CoreRequestHeader}.
 * 
 * Note that this is the only concrete implementation of the
 * {@link RequestHeader} class for the Java API, and that's why the equality
 * contract is implemented here in terms of {@link InternalRequestHeader}. No
 * other concrete kind of instances are expected for {@link RequestHeader}.
 * 
 * This class is not expected to be instantiated by users.
 */
public final class InternalRequestHeader extends RequestHeader {

  public InternalRequestHeader(CoreRequestHeader delegate) {
    super(delegate);
  }

  public InternalRequestHeader(Method method, URI uri, MessageProtocol protocol,
      PSequence<MessageProtocol> acceptedResponseProtocols, Optional<Principal> principal,
      PMap<String, PSequence<String>> headers) {
    super(method, uri, protocol, acceptedResponseProtocols, principal, headers);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof InternalRequestHeader))
      return false;

    InternalRequestHeader that = (InternalRequestHeader) o;
    return delegate.equals(that.delegate);
  }

  public static CoreRequestHeader obtainDelegate(RequestHeader request) {
    return ((InternalRequestHeader) request).delegate;
  }

}
