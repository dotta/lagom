/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api

import java.lang.reflect.Type
import scala.collection.immutable.Seq
import com.lightbend.lagom.api.transport.Method
import com.lightbend.lagom.internal.api.deser._
import com.lightbend.lagom.internal.api.security.CoreUserAgentServiceIdentificationStrategy
import com.lightbend.lagom.internal.api.transport.CoreHeaderTransformer
import com.lightbend.lagom.internal.api.transport.CorePathVersionedProtocolNegotiationStrategy
import com.lightbend.lagom.spi.CircuitBreakerId;

/**
 * Describes a service.
 *
 * A descriptor is a set of calls descriptors that the service provides, coupled with metadata about how the
 * service and its calls are to be served.  Metadata may include versioning and migrations, SLA's, sharding
 * hints, circuit breaker strategies etc.
 */
final case class CoreDescriptor(
  name:                          String,
  calls:                         Seq[CoreDescriptor.Call[_, _, _]],
  idSerializers:                 Map[Type, CoreIdSerializer[_]],
  messageSerializers:            Map[Type, CoreMessageSerializer[_, _]],
  serializerFactory:             CoreSerializerFactory,
  exceptionSerializer:           CoreExceptionSerializer,
  autoAcl:                       Boolean,
  acls:                          Seq[CoreServiceAcl],
  protocolNegotiationStrategy:   CoreHeaderTransformer,
  serviceIdentificationStrategy: CoreHeaderTransformer,
  locatableService:              Boolean
) {

  def withSerializer[T](idType: Class[T], idSerializer: CoreIdSerializer[T]): CoreDescriptor =
    withSerializer(idType.asInstanceOf[Type], idSerializer)

  def withSerializer(idType: Type, idSerializer: CoreIdSerializer[_]): CoreDescriptor = {
    copy(idSerializers = idSerializers + (idType -> idSerializer))
  }

  def withSerializer[T](messageType: Class[T], messageSerializer: CoreMessageSerializer[T, _]): CoreDescriptor =
    withSerializer(messageType.asInstanceOf[Type], messageSerializer)

  def withSerializer(messageType: Type, messageSerializer: CoreMessageSerializer[_, _]): CoreDescriptor =
    replaceAllMessageSerializers(messageSerializers + (messageType -> messageSerializer))

  def withCalls(calls: Seq[CoreDescriptor.Call[_, _, _]]): CoreDescriptor = replaceAllCalls(this.calls ++ calls)

  def replaceAllCalls(calls: Seq[CoreDescriptor.Call[_, _, _]]): CoreDescriptor = copy(calls = calls)

  def replaceAllIdSerializers(idSerializers: Map[Type, CoreIdSerializer[_]]): CoreDescriptor =
    copy(idSerializers = idSerializers)

  def replaceAllMessageSerializers(messageSerializers: Map[Type, CoreMessageSerializer[_, _]]): CoreDescriptor =
    copy(messageSerializers = messageSerializers)

  def withSerializer(exceptionSerializer: CoreExceptionSerializer): CoreDescriptor =
    copy(exceptionSerializer = exceptionSerializer)

  /**
   * Set whether the service calls in this descriptor should default to having an ACL automatically generated for
   * them.
   *
   * By default, this will not happen.
   *
   * Note that each service call can override this by calling withAutoAcl on them.
   *
   * @param autoAcl Whether autoAcl should be true.
   * @return A copy of this descriptor.
   */
  def withAutoAcl(autoAcl: Boolean): CoreDescriptor = copy(autoAcl = autoAcl)

  /**
   * Add the given manual ACLs.
   *
   * If auto ACLs are configured, these will be added in addition to the auto ACLs.
   *
   * @param acls The ACLs to add.
   * @return A copy of this descriptor.
   */
  def withAcls(acls: Seq[CoreServiceAcl]): CoreDescriptor = replaceAllAcls(this.acls ++ acls)

  /**
   * Replace all the ACLs with the given ACL sequence.
   *
   * This will not replace ACLs generated by autoAcl, to disable autoAcl, turn it off.
   *
   * @param acls The ACLs to use.
   * @return A copy of this descriptor.
   */
  def replaceAllAcls(acls: Seq[CoreServiceAcl]): CoreDescriptor = copy(acls = acls)

  def withProtocolNegotiationStrategy(protocolNegotiationStrategy: CoreHeaderTransformer): CoreDescriptor =
    copy(protocolNegotiationStrategy = protocolNegotiationStrategy)

  def withServiceIdentificationStrategy(serviceIdentificationStrategy: CoreHeaderTransformer): CoreDescriptor =
    copy(serviceIdentificationStrategy = serviceIdentificationStrategy)

  /**
   * Set whether this service is locatable.
   *
   * Locatable services are registered with the service locator and/or gateway, so that they can be consumed by other
   * services.  Services that are not locatable are typically services for infrastructure purposes, such as providing
   * metrics.
   *
   * @param locatableService Whether this service should be locatable or not.
   * @return A copy of this descriptor.
   */
  def withLocatableService(locatableService: Boolean): CoreDescriptor = copy(locatableService = locatableService)

}

object CoreDescriptor {
  def apply(name: String): CoreDescriptor = new CoreDescriptor(
    name,
    calls = Seq.empty,
    idSerializers = Map.empty,
    messageSerializers = Map.empty,
    serializerFactory = CoreSerializerFactory.PlaceholderSerializerFactory,
    exceptionSerializer = CoreExceptionSerializer.PlaceholderExceptionSerializer,
    autoAcl = false,
    acls = Seq.empty,
    CorePathVersionedProtocolNegotiationStrategy(),
    CoreUserAgentServiceIdentificationStrategy(),
    locatableService = true
  )

  /**
   * Describes a service call.
   */
  case class Call[Id, Request, Response](callId: CallId)(
    // note: currying because all the following parameters are not part of the equality contract
    val serviceCall:        CoreServiceCall[Id, Request, Response],
    val idSerializer:       CoreIdSerializer[Id],
    val requestSerializer:  CoreMessageSerializer[Request, _],
    val responseSerializer: CoreMessageSerializer[Response, _],
    val circuitBreaker:     Option[CircuitBreakerId],
    val autoAcl:            Option[Boolean]
  ) {

    /**
     * Return a copy of this call descriptor with the given ID serializer configured.
     *
     * @param idSerializer
     *          The ID serializer.
     * @return A copy of this call descriptor.
     */
    def copyWith(idSerializer: CoreIdSerializer[Id]): Call[Id, Request, Response] =
      Call(callId)(serviceCall, idSerializer, requestSerializer, responseSerializer, circuitBreaker, autoAcl)

    /**
     * Return a copy of this call descriptor with the given request message
     * serializer configured.
     *
     * @param requestSerializer
     *          The request serializer.
     * @return A copy of this call descriptor.
     */
    def replaceRequestSerializer(requestSerializer: CoreMessageSerializer[Request, _]): Call[Id, Request, Response] =
      Call(callId)(serviceCall, idSerializer, requestSerializer, responseSerializer, circuitBreaker, autoAcl)

    /**
     * Return a copy of this call descriptor with the given response message
     * serializer configured.
     *
     * @param responseSerializer
     *          The response serializer.
     * @return A copy of this call descriptor.
     */
    def replaceResponseSerializer(responseSerializer: CoreMessageSerializer[Response, _]): Call[Id, Request, Response] =
      Call(callId)(serviceCall, idSerializer, requestSerializer, responseSerializer, circuitBreaker, autoAcl)

    /**
     * Return a copy of this call descriptor with the given service call
     * configured.
     *
     * @param serviceCall
     *          The service call.
     * @return A copy of this call descriptor.
     */
    def copyWith(serviceCall: CoreServiceCall[Id, Request, Response]): Call[Id, Request, Response] =
      Call(callId)(serviceCall, idSerializer, requestSerializer, responseSerializer, circuitBreaker, autoAcl)

    /**
     * Return a copy of this call descriptor with the given circuit breaker
     * identifier configured.
     *
     * @param breakerId
     *          The configuration id of the circuit breaker
     * @return A copy of this call descriptor.
     */
    def copyWith(circuitBreaker: CircuitBreakerId): Call[Id, Request, Response] =
      Call(callId)(serviceCall, idSerializer, requestSerializer, responseSerializer, Option(circuitBreaker), autoAcl)

    /**
     * Return a copy of this call descriptor with autoAcl configured.
     *
     * @param autoAcl Whether an ACL will automatically be generated for the gateway to route calls to this service.
     * @return A copy of this call descriptor.
     */
    def copyWith(autoAcl: Boolean): Call[Id, Request, Response] =
      Call(callId)(serviceCall, idSerializer, requestSerializer, responseSerializer, circuitBreaker, Option(autoAcl))

    override def toString: String =
      s"Call{callId=$callId, serviceCall=$serviceCall, idSerializer=$idSerializer, " +
        s"requestSerializer=$requestSerializer, responseSerializer=$responseSerializer, " +
        s"circuitBreaker=$circuitBreaker, autoAcl=$autoAcl}"
  }

  /**
   * The Call ID.
   *
   * This is an abstract representation of how a service call is addressed within a service.  For example, in the
   * case of REST APIs, it will be addressed using an HTTP method and path.
   */
  sealed abstract class CallId

  /**
   * A REST call ID.
   */
  case class RestCallId(method: Method, pathPattern: String) extends CallId {
    override def toString: String = s"RestCallId{method=$method, pathPattern='$pathPattern'}"
  }

  /**
   * A path based call ID.
   */
  case class PathCallId(pathPattern: String) extends CallId {
    override def toString: String = s"PathCallId{pathPattern='$pathPattern'}"
  }

  /**
   * A named call ID.
   */
  case class NamedCallId(name: String) extends CallId {

    override def toString: String =
      s"NamedCallId{name='$name'}"
  }
}