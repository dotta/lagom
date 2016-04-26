/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.javadsl.api;

import java.util.Optional;

import com.lightbend.lagom.api.transport.Method;
import com.lightbend.lagom.internal.api.CallIdConverter;
import com.lightbend.lagom.internal.api.CoreDescriptor;
import com.lightbend.lagom.internal.api.deser.AdapterIdSerializer;
import com.lightbend.lagom.internal.api.deser.AdapterMessageSerializer;
import com.lightbend.lagom.javadsl.api.deser.*;
import com.lightbend.lagom.javadsl.api.security.UserAgentServiceIdentificationStrategy;
import com.lightbend.lagom.javadsl.api.transport.HeaderTransformer;
import com.lightbend.lagom.javadsl.api.transport.PathVersionedProtocolNegotiationStrategy;
import com.lightbend.lagom.spi.CircuitBreakerId;
import org.pcollections.*;
import java.lang.reflect.Type;
import java.util.Arrays;
import scala.compat.java8.FunctionConverters;
import scala.compat.java8.OptionConverters;

/**
 * Describes a service.
 *
 * A descriptor is a set of calls descriptors that the service provides, coupled
 * with metadata about how the service and its calls are to be served. Metadata
 * may include versioning and migrations, SLA's, sharding hints, circuit breaker
 * strategies etc.
 */
public final class Descriptor {

  private final CoreDescriptor delegate;

  private Descriptor(CoreDescriptor delegate) {
    this.delegate = delegate;
  }

  /**
   * Describes a service call.
   */
  public static final class Call<Id, Request, Response> {

    private final CoreDescriptor.Call<Id, Request, Response> delegate;

    private Call(CoreDescriptor.Call<Id, Request, Response> delegate) {
      this.delegate = delegate;
    }

    Call(CallId callId, ServiceCall<Id, Request, Response> serviceCall,
             IdSerializer<Id> idSerializer, MessageSerializer<Request, ?> requestSerializer,
             MessageSerializer<Response, ?> responseSerializer, Optional<CircuitBreakerId> circuitBreaker,
             Optional<Boolean> autoAcl) {
          this(CoreDescriptor.Call.<Id, Request, Response>apply(
            callId.obtainDelegate(),
            CoreServiceCall.apply((id, request) -> serviceCall.invoke(id, request)),
            new AdapterIdSerializer(idSerializer),
            new AdapterMessageSerializer<Request, ?>(requestSerializer),
            new AdapterMessageSerializer<Response, ?>(responseSerializer),
            OptionConverters.asScala(circuitBreaker),
            OptionConverters.asScala(autoAcl)));
        }

    /**
     * Get the id for the call.
     *
     * @return The id.
     */
    public CallId callId() {
      return CallIdConverter.asApi(delegate.callId());
    }

    /**
     * Get the service call itself.
     *
     * @return The service call.
     */
    public ServiceCall<Id, Request, Response> serviceCall() {
      return (id, request) -> delegate.serviceCall().apply(id, request);
    }

    /**
     * Get the ID serializer.
     *
     * @return The ID serializer.
     */
    @SuppressWarnings("unchecked")
    public IdSerializer<Id> idSerializer() {
      return ((AdapterIdSerializer<Id>) delegate.idSerializer()).idSerializer();
    }

    /**
     * Get the request message serializer.
     *
     * @return The request serializer.
     */
    @SuppressWarnings("unchecked")
    public MessageSerializer<Request, ?> requestSerializer() {
      return ((AdapterMessageSerializer<Request, ?>) delegate.requestSerializer()).serializer();
    }

    /**
     * Get the response message serializer.
     *
     * @return The response serializer.
     */
    @SuppressWarnings("unchecked")
    public MessageSerializer<Response, ?> responseSerializer() {
      return ((AdapterMessageSerializer<Response, ?>) delegate.responseSerializer()).serializer();
    }

    /**
     * Get the circuit breaker identifier.
     *
     * @return The circuit breaker identifier.
     */
    public Optional<CircuitBreakerId> circuitBreaker() {
      return OptionConverters.toJava(delegate.circuitBreaker());
    }

    /**
     * Whether this service call should automatically define an ACL for the
     * router to route external calls to it.
     *
     * @return Some value if this service call explicitly decides that it should
     *         have an auto ACL defined for it, otherwise empty.
     */
    public Optional<Boolean> autoAcl() {
      return OptionConverters.toJava(delegate.autoAcl());
    }

    /**
     * Return a copy of this call descriptor with the given ID serializer
     * configured.
     *
     * @param idSerializer
     *          The ID serializer.
     * @return A copy of this call descriptor.
     */
    public Call<Id, Request, Response> with(IdSerializer<Id> idSerializer) {
      return new Call<>(delegate.copyWith(new AdapterIdSerializer<Id>(idSerializer)));
    }

    /**
     * Return a copy of this call descriptor with the given request message
     * serializer configured.
     *
     * @param requestSerializer
     *          The request serializer.
     * @return A copy of this call descriptor.
     */
    public Call<Id, Request, Response> withRequestSerializer(MessageSerializer<Request, ?> requestSerializer) {
      AdapterMessageSerializer<Request, ?> serializer = new AdapterMessageSerializer<>(requestSerializer);
      return new Call<>(delegate.replaceRequestSerializer(serializer));
    }

    /**
     * Return a copy of this call descriptor with the given response message
     * serializer configured.
     *
     * @param responseSerializer
     *          The response serializer.
     * @return A copy of this call descriptor.
     */
    public Call<Id, Request, Response> withResponseSerializer(MessageSerializer<Response, ?> responseSerializer) {
      AdapterMessageSerializer<Response, ?> serializer = new AdapterMessageSerializer<>(responseSerializer);
      return new Call<>(delegate.replaceResponseSerializer(serializer));
    }

    /**
     * Return a copy of this call descriptor with the given service call
     * configured.
     *
     * @param serviceCall
     *          The service call.
     * @return A copy of this call descriptor.
     */
    public Call<Id, Request, Response> with(ServiceCall<Id, Request, Response> serviceCall) {
      return new Call<>(delegate.copyWith(serviceCall));
    }

    /**
     * Return a copy of this call descriptor with the given circuit breaker
     * identifier configured.
     *
     * @param breakerId
     *          The configuration id of the circuit breaker
     * @return A copy of this call descriptor.
     */
    public Call<Id, Request, Response> withCircuitBreaker(CircuitBreakerId breakerId) {
      return new Call<>(delegate.copyWith(breakerId));
    }

    /**
     * Return a copy of this call descriptor with autoAcl configured.
     *
     * @param autoAcl
     *          Whether an ACL will automatically be generated for the gateway
     *          to route calls to this service.
     * @return A copy of this call descriptor.
     */
    public Call<Id, Request, Response> withAutoAcl(boolean autoAcl) {
      return new Call<>(delegate.copyWith(autoAcl));
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (!(o instanceof Call))
        return false;

      Call<?, ?, ?> that = (Call<?, ?, ?>) o;

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

  /**
   * The Call ID.
   *
   * This is an abstract representation of how a service call is addressed
   * within a service. For example, in the case of REST APIs, it will be
   * addressed using an HTTP method and path.
   */
  public static abstract class CallId {
    private CallId() {}
    abstract CoreDescriptor.CallId obtainDelegate(); 
  }

  /**
   * A REST call ID.
   */
  public static final class RestCallId extends CallId {
    private final CoreDescriptor.RestCallId delegate;

    private RestCallId(CoreDescriptor.RestCallId delegate) {
      this.delegate = delegate;
    }

    public RestCallId(Method method, String pathPattern) {
      this(CoreDescriptor.RestCallId.apply(method, pathPattern));
    }

    @Override
    CoreDescriptor.RestCallId obtainDelegate() {
      return delegate;
    }

    /**
     * The HTTP method for the call.
     *
     * The method will only be used for strict REST calls. For other calls, such
     * as calls implemented by WebSockets or other transports, the method may be
     * ignored completely.
     *
     * @return The HTTP method.
     */
    public Method method() {
      return delegate.method();
    }

    /**
     * The path pattern for the call.
     *
     * @return The path pattern.
     */
    public String pathPattern() {
      return delegate.pathPattern();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (!(o instanceof RestCallId))
        return false;

      RestCallId that = (RestCallId) o;

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

  /**
   * A path based call ID.
   */
  public static class PathCallId extends CallId {
    private final CoreDescriptor.PathCallId delegate;

    private PathCallId(CoreDescriptor.PathCallId delegate) {
      this.delegate = delegate;
    }

    public PathCallId(String pathPattern) {
      this(CoreDescriptor.PathCallId.apply(pathPattern));
    }

    @Override
    CoreDescriptor.PathCallId obtainDelegate() {
      return delegate;
    }

    /**
     * Get the path pattern.
     *
     * @return The path pattern.
     */
    public String pathPattern() {
      return delegate.pathPattern();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (!(o instanceof PathCallId))
        return false;

      PathCallId that = (PathCallId) o;

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

  /**
   * A named call ID.
   */
  public static class NamedCallId extends CallId {
    private final CoreDescriptor.NamedCallId delegate;

    public NamedCallId(CoreDescriptor.NamedCallId delegate) {
      this.delegate = delegate;
    }

    public NamedCallId(String name) {
      this(CoreDescriptor.NamedCallId.apply(name));
    }

    @Override
    CoreDescriptor.NamedCallId obtainDelegate() {
      return delegate;
    }

    /**
     * Get the name of the call.
     *
     * @return The name of the call.
     */
    public String name() {
      return delegate.name();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (!(o instanceof NamedCallId))
        return false;

      NamedCallId that = (NamedCallId) o;

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

  private final String name;
  private final PSequence<Call<?, ?, ?>> calls;
  private final PMap<Type, IdSerializer<?>> idSerializers;
  private final PMap<Type, MessageSerializer<?, ?>> messageSerializers;
  private final SerializerFactory serializerFactory;
  private final ExceptionSerializer exceptionSerializer;
  private final boolean autoAcl;
  private final PSequence<ServiceAcl> acls;
  private final HeaderTransformer protocolNegotiationStrategy;
  private final HeaderTransformer serviceIdentificationStrategy;
  private final boolean locatableService;

  Descriptor(String name) {
    this(name, TreePVector.empty(), HashTreePMap.empty(), HashTreePMap.empty(), SerializerFactory.DEFAULT,
        ExceptionSerializer.DEFAULT, false, TreePVector.empty(), new PathVersionedProtocolNegotiationStrategy(),
        new UserAgentServiceIdentificationStrategy(), true);
  }

  Descriptor(String name, PSequence<Call<?, ?, ?>> calls, PMap<Type, IdSerializer<?>> idSerializers,
      PMap<Type, MessageSerializer<?, ?>> messageSerializers, SerializerFactory serializerFactory,
      ExceptionSerializer exceptionSerializer, boolean autoAcl, PSequence<ServiceAcl> acls,
      HeaderTransformer protocolNegotiationStrategy, HeaderTransformer serviceIdentificationStrategy,
      boolean locatableService) {
    this.name = name;
    this.calls = calls;
    this.idSerializers = idSerializers;
    this.messageSerializers = messageSerializers;
    this.serializerFactory = serializerFactory;
    this.exceptionSerializer = exceptionSerializer;
    this.autoAcl = autoAcl;
    this.acls = acls;
    this.protocolNegotiationStrategy = protocolNegotiationStrategy;
    this.serviceIdentificationStrategy = serviceIdentificationStrategy;
    this.locatableService = locatableService;
  }

  public String name() {
    return name;
  }

  public PSequence<Call<?, ?, ?>> calls() {
    return calls;
  }

  public PMap<Type, IdSerializer<?>> idSerializers() {
    return idSerializers;
  }

  public PMap<Type, MessageSerializer<?, ?>> messageSerializers() {
    return messageSerializers;
  }

  public SerializerFactory serializerFactory() {
    return serializerFactory;
  }

  public ExceptionSerializer exceptionSerializer() {
    return exceptionSerializer;
  }

  /**
   * Whether this descriptor will auto generate ACLs for each call.
   */
  public boolean autoAcl() {
    return autoAcl;
  }

  /**
   * The manually configured ACLs for this service.
   */
  public PSequence<ServiceAcl> acls() {
    return acls;
  }

  public HeaderTransformer protocolNegotiationStrategy() {
    return protocolNegotiationStrategy;
  }

  public HeaderTransformer serviceIdentificationStrategy() {
    return serviceIdentificationStrategy;
  }

  /**
   * Whether this is a locatable service.
   *
   * Locatable services are registered with the service locator and/or gateway,
   * so that they can be consumed by other services. Services that are not
   * locatable are typically services for infrastructure purposes, such as
   * providing metrics.
   */
  public boolean locatableService() {
    return locatableService;
  }

  public <T> Descriptor with(Class<T> idType, IdSerializer<T> idSerializer) {
    return with((Type) idType, idSerializer);
  }

  public Descriptor with(Type idType, IdSerializer<?> idSerializer) {
    return replaceAllIdSerializers(idSerializers.plus(idType, idSerializer));
  }

  public <T> Descriptor with(Class<T> messageType, MessageSerializer<T, ?> messageSerializer) {
    return with((Type) messageType, messageSerializer);
  }

  public Descriptor with(Type messageType, MessageSerializer<?, ?> messageSerializer) {
    return replaceAllMessageSerializers(messageSerializers.plus(messageType, messageSerializer));
  }

  public Descriptor with(Call<?, ?, ?>... calls) {
    return replaceAllCalls(this.calls.plusAll(Arrays.asList(calls)));
  }

  public Descriptor replaceAllCalls(PSequence<Call<?, ?, ?>> calls) {
    return new Descriptor(name, calls, idSerializers, messageSerializers, serializerFactory, exceptionSerializer,
        autoAcl, acls, protocolNegotiationStrategy, serviceIdentificationStrategy, locatableService);
  }

  public Descriptor replaceAllIdSerializers(PMap<Type, IdSerializer<?>> idSerializers) {
    return new Descriptor(name, calls, idSerializers, messageSerializers, serializerFactory, exceptionSerializer,
        autoAcl, acls, protocolNegotiationStrategy, serviceIdentificationStrategy, locatableService);
  }

  public Descriptor replaceAllMessageSerializers(PMap<Type, MessageSerializer<?, ?>> messageSerializers) {
    return new Descriptor(name, calls, idSerializers, messageSerializers, serializerFactory, exceptionSerializer,
        autoAcl, acls, protocolNegotiationStrategy, serviceIdentificationStrategy, locatableService);
  }

  public Descriptor with(ExceptionSerializer exceptionSerializer) {
    return new Descriptor(name, calls, idSerializers, messageSerializers, serializerFactory, exceptionSerializer,
        autoAcl, acls, protocolNegotiationStrategy, serviceIdentificationStrategy, locatableService);
  }

  /**
   * Set whether the service calls in this descriptor should default to having
   * an ACL automatically generated for them.
   *
   * By default, this will not happen.
   *
   * Note that each service call can override this by calling withAutoAcl on
   * them.
   *
   * @param autoAcl
   *          Whether autoAcl should be true.
   * @return A copy of this descriptor.
   */
  public Descriptor withAutoAcl(boolean autoAcl) {
    return new Descriptor(name, calls, idSerializers, messageSerializers, serializerFactory, exceptionSerializer,
        autoAcl, acls, protocolNegotiationStrategy, serviceIdentificationStrategy, locatableService);
  }

  /**
   * Add the given manual ACLs.
   *
   * If auto ACLs are configured, these will be added in addition to the auto
   * ACLs.
   *
   * @param acls
   *          The ACLs to add.
   * @return A copy of this descriptor.
   */
  public Descriptor with(ServiceAcl... acls) {
    return replaceAllAcls(this.acls.plusAll(Arrays.asList(acls)));
  }

  /**
   * Replace all the ACLs with the given ACL sequence.
   *
   * This will not replace ACLs generated by autoAcl, to disable autoAcl, turn
   * it off.
   *
   * @param acls
   *          The ACLs to use.
   * @return A copy of this descriptor.
   */
  public Descriptor replaceAllAcls(PSequence<ServiceAcl> acls) {
    return new Descriptor(name, calls, idSerializers, messageSerializers, serializerFactory, exceptionSerializer,
        autoAcl, acls, protocolNegotiationStrategy, serviceIdentificationStrategy, locatableService);
  }

  public Descriptor withProtocolNegotiationStrategy(HeaderTransformer protocolNegotiationStrategy) {
    return new Descriptor(name, calls, idSerializers, messageSerializers, serializerFactory, exceptionSerializer,
        autoAcl, acls, protocolNegotiationStrategy, serviceIdentificationStrategy, locatableService);
  }

  public Descriptor withServiceIdentificationStrategy(HeaderTransformer serviceIdentificationStrategy) {
    return new Descriptor(name, calls, idSerializers, messageSerializers, serializerFactory, exceptionSerializer,
        autoAcl, acls, protocolNegotiationStrategy, serviceIdentificationStrategy, locatableService);
  }

  /**
   * Set whether this service is locatable.
   *
   * Locatable services are registered with the service locator and/or gateway,
   * so that they can be consumed by other services. Services that are not
   * locatable are typically services for infrastructure purposes, such as
   * providing metrics.
   *
   * @param locatableService
   *          Whether this service should be locatable or not.
   * @return A copy of this descriptor.
   */
  public Descriptor withLocatableService(boolean locatableService) {
    return new Descriptor(name, calls, idSerializers, messageSerializers, serializerFactory, exceptionSerializer,
        autoAcl, acls, protocolNegotiationStrategy, serviceIdentificationStrategy, locatableService);
  }

}
