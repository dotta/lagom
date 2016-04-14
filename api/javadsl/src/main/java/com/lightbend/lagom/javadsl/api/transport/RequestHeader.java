/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.javadsl.api.transport;

import org.pcollections.PMap;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import com.lightbend.lagom.api.transport.Method;
import com.lightbend.lagom.internal.api.transport.CoreMessageProtocol;
import com.lightbend.lagom.internal.api.transport.CoreRequestHeader;
import com.lightbend.lagom.internal.api.transport.InternalRequestHeader;

import java.net.URI;
import java.security.Principal;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import scala.compat.java8.OptionConverters;
import scala.compat.java8.ScalaStreamSupport;

/**
 * A request header.
 *
 * This header may or may not be mapped down onto HTTP. In order to remain
 * agnostic to the underlying protocol, information required by Lagom, such as
 * protocol information, is extracted. It is encouraged that the protocol
 * information always be used in preference to reading the information directly
 * out of headers, since the headers may not contain the necessary protocol
 * information.
 *
 * The headers are however still provided, in case information needs to be
 * extracted out of non standard headers.
 * 
 * Note: This class is not meant to be extended or instantiated by clients.
 */
public abstract class RequestHeader extends MessageHeader {

  /**
   * A default request header object.
   *
   * This is a convenience supplied so that server implementations of service
   * calls can pass this request to the request header handler, in order to get
   * the actual incoming request header.
   *
   * See
   * {@link com.lightbend.lagom.javadsl.api.ServiceCall#handleRequestHeader(Function)}
   */
  public static final RequestHeader DEFAULT = new InternalRequestHeader(CoreRequestHeader.Default());

  protected final CoreRequestHeader delegate;

  protected RequestHeader(CoreRequestHeader delegate) {
    super(delegate);
    this.delegate = delegate;
  }

  protected RequestHeader(Method method, URI uri, MessageProtocol protocol,
      PSequence<MessageProtocol> acceptedResponseProtocols, Optional<Principal> principal,
      PMap<String, PSequence<String>> headers) {
    this(CoreRequestHeader.of(method, uri, protocol.delegate,
        acceptedResponseProtocols.stream().map(p -> p.delegate), principal, headers));
  }

  /**
   * Get the method used to make this request.
   *
   * @return The method.
   */
  public Method method() {
    return delegate.method();
  }

  /**
   * Get the URI for this request.
   *
   * @return The URI.
   */
  public URI uri() {
    return delegate.uri();
  }

  /**
   * Get the accepted response protocols for this request.
   *
   * @return The accepted response protocols.
   */
  public PSequence<MessageProtocol> acceptedResponseProtocols() {
    Stream<MessageProtocol> adapted = ScalaStreamSupport.stream(delegate.acceptedResponseProtocols())
        .map(MessageProtocol::new);
    return TreePVector.from(adapted.collect(Collectors.toList()));
  }

  /**
   * Get the principal for this request, if there is one.
   *
   * @return The principal for this request.
   */
  public Optional<Principal> principal() {
    return OptionConverters.toJava(delegate.principal());
  }

  /**
   * Return a copy of this request header with the given method set.
   *
   * @param method
   *          The method to set.
   * @return A copy of this request header.
   */
  public RequestHeader withMethod(Method method) {
    return new InternalRequestHeader(delegate.withMethod(method));
  }

  /**
   * Return a copy of this request header with the given uri set.
   *
   * @param uri
   *          The uri to set.
   * @return A copy of this request header.
   */
  public RequestHeader withUri(URI uri) {
    return new InternalRequestHeader(delegate.withUri(uri));
  }

  @Override
  public RequestHeader withProtocol(MessageProtocol protocol) {
    return new InternalRequestHeader(delegate.withProtocol(protocol.delegate));
  }

  /**
   * Return a copy of this request header with the given accepted response
   * protocols set.
   *
   * @param acceptedResponseProtocols
   *          The accepted response protocols to set.
   * @return A copy of this request header.
   */
  public RequestHeader withAcceptedResponseProtocols(PSequence<MessageProtocol> acceptedResponseProtocols) {
    Stream<CoreMessageProtocol> adapted = acceptedResponseProtocols.stream()
        .map(p -> p.delegate);
    return new InternalRequestHeader(delegate.withAcceptedResponseProtocols(adapted));
  }

  /**
   * Return a copy of this request header with the principal set.
   *
   * @param principal
   *          The principal to set.
   * @return A copy of this request header.
   */
  public RequestHeader withPrincipal(Principal principal) {
    return new InternalRequestHeader(delegate.withPrincipal(principal));
  }

  /**
   * Return a copy of this request header with the principal cleared.
   *
   * @return A copy of this request header.
   */
  public RequestHeader clearPrincipal() {
    return withPrincipal(null);
  }

  @Override
  public RequestHeader replaceAllHeaders(PMap<String, PSequence<String>> headers) {
    return new InternalRequestHeader(delegate.replaceAllHeaders(headers));
  }

  @Override
  public RequestHeader withHeader(String name, String value) {
    return new InternalRequestHeader(delegate.withHeader(name, value));
  }

  @Override
  public String toString() {
    return delegate.toString();
  }

  @Override
  public int hashCode() {
    return delegate.hashCode();
  }
}
