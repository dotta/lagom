/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api

import java.lang.reflect.Type
import java.util
import akka.stream.javadsl.Source
import akka.util.ByteString
import com.lightbend.lagom.internal.api.deser.CoreMessageSerializer.{ NegotiatedDeserializer, NegotiatedSerializer }
import com.lightbend.lagom.internal.api.deser._
import com.lightbend.lagom.internal.api.transport.CoreMessageProtocol

import scala.collection.immutable.Seq

trait PlaceholderSerializerFactory extends CoreSerializerFactory {
  override def messageSerializerFor[MessageEntity](`type`: Type): CoreMessageSerializer[MessageEntity, _] =
    throw new NotImplementedError("This serializer factory is only a placeholder, and cannot be used directly")
}

case object JacksonPlaceholderSerializerFactory extends PlaceholderSerializerFactory

trait UnresolvedMessageSerializer[MessageEntity] extends CoreMessageSerializer[MessageEntity, Any] {
  override def serializerForRequest: NegotiatedSerializer[MessageEntity, Any] =
    throw new NotImplementedError("Cannot use unresolved message serializer")

  override def deserializer(messageHeader: CoreMessageProtocol): NegotiatedDeserializer[MessageEntity, Any] =
    throw new NotImplementedError("Cannot use unresolved message serializer")

  override def serializerForResponse(acceptedMessageHeaders: Seq[CoreMessageProtocol]): NegotiatedSerializer[MessageEntity, Any] =
    throw new NotImplementedError("Cannot use unresolved message serializer")

  def resolve(factory: CoreSerializerFactory, typeInfo: Option[Type]): CoreMessageSerializer[MessageEntity, _]
}

class UnresolvedMessageTypeSerializer[MessageEntity](val entityType: Type) extends UnresolvedMessageSerializer[MessageEntity] {
  override def resolve(factory: CoreSerializerFactory, typeInfo: Option[Type]): CoreMessageSerializer[MessageEntity, _] =
    factory.messageSerializerFor(entityType)
}

class UnresolvedStreamedMessageSerializer[MessageEntity](val messageType: Type) extends UnresolvedMessageSerializer[Source[MessageEntity, _]] {
  override def resolve(factory: CoreSerializerFactory, typeInfo: Option[Type]): CoreMessageSerializer[Source[MessageEntity, _], _] =
    factory.messageSerializerFor[MessageEntity](messageType) match {
      case strict: CoreStrictMessageSerializer[MessageEntity] => new DelegatingStreamedMessageSerializer[MessageEntity](strict)
      case other => throw new IllegalArgumentException("Can't create streamed message serializer that delegates to " + other)
    }
}

trait PlaceholderExceptionSerializer extends CoreExceptionSerializer {
  override def serialize(exception: Throwable, accept: Seq[CoreMessageProtocol]): CoreRawExceptionMessage =
    throw new UnsupportedOperationException("This serializer is only a placeholder, and cannot be used directly: " + this, exception)

  override def deserialize(message: CoreRawExceptionMessage): Throwable =
    throw new UnsupportedOperationException("This serializer is only a placeholder, and cannot be used directly: " + this + ", trying te deserialize: " + message)
}

case object JacksonPlaceholderExceptionSerializer extends PlaceholderExceptionSerializer

class DelegatingStreamedMessageSerializer[MessageEntity](delegate: CoreStrictMessageSerializer[MessageEntity]) extends CoreStreamedMessageSerializer[MessageEntity] {

  private class DelegatingStreamedSerializer(delegate: NegotiatedSerializer[MessageEntity, ByteString]) extends NegotiatedSerializer[Source[MessageEntity, _], Source[ByteString, _]] {
    override def protocol: CoreMessageProtocol = delegate.protocol
    override def serialize(source: Source[MessageEntity, _]): Source[ByteString, _] = {
      source.asScala.map(delegate.serialize).asJava
    }
  }

  private class DelegatingStreamedDeserializer(delegate: NegotiatedDeserializer[MessageEntity, ByteString]) extends NegotiatedDeserializer[Source[MessageEntity, _], Source[ByteString, _]] {
    override def deserialize(source: Source[ByteString, _]): Source[MessageEntity, _] = {
      source.asScala.map(delegate.deserialize).asJava
    }
  }

  override def acceptResponseProtocols: Seq[CoreMessageProtocol] = delegate.acceptResponseProtocols

  override def serializerForRequest: NegotiatedSerializer[Source[MessageEntity, _], Source[ByteString, _]] =
    new DelegatingStreamedSerializer(delegate.serializerForRequest)

  override def deserializer(messageHeader: CoreMessageProtocol): NegotiatedDeserializer[Source[MessageEntity, _], Source[ByteString, _]] = {
    new DelegatingStreamedDeserializer(delegate.deserializer(messageHeader))
  }

  override def serializerForResponse(acceptedMessageHeaders: Seq[CoreMessageProtocol]): NegotiatedSerializer[Source[MessageEntity, Any], Source[ByteString, Any]] = {
    new DelegatingStreamedSerializer(delegate.serializerForResponse(acceptedMessageHeaders))
  }
}
