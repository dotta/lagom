/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api.deser

import com.lightbend.lagom.api.deser.SerializationException
import com.lightbend.lagom.api.deser.DeserializationException
import com.lightbend.lagom.api.deser.DeserializationException
import com.lightbend.lagom.api.deser.SerializationException
import com.lightbend.lagom.internal.api.transport.CoreMessageProtocol
import com.lightbend.lagom.api.transport.UnsupportedMediaType
import com.lightbend.lagom.api.transport.NotAcceptable

import scala.collection.immutable.Seq

/**
 * Serializer for messages.
 *
 * A message serializer is effectively a factory for negotiating serializers/deserializers.  They are created by passing
 * the relevant protocol information to then decide on a serializer/deserializer to use.
 *
 * The returned serializers/deserializers may be invoked once for strict messages, or many times for streamed messages.
 *
 * This interface doesn't actually specify the wireformat that the serializer must serialize to, there are two sub
 * interfaces that do, they being {@link StrictMessageSerializer}, which serializes messages that are primarily in
 * memory, to and from {@link ByteString}, and {@link StreamedMessageSerializer}, which serializes streams of messages.
 * Note that all message serializers used by the framework must implement one of these two sub interfaces, the
 * framework does not know how to handle other serializer types.
 *
 * @param <MessageEntity> The message entity being serialized/deserialized.
 */
trait CoreMessageSerializer[MessageEntity, WireFormat] {

  /**
   * The message headers that will be accepted for response serialization.
   */
  def acceptResponseProtocols: Seq[CoreMessageProtocol] = Seq.empty

  /**
   * Whether this serializer serializes values that are used or not.
   *
   * If false, it means this serializer is for an empty request/response, eg, they use the
   * {@link akka.NotUsed} type.
   *
   * @return Whether the values this serializer serializes are used.
   */
  def isUsed: Boolean = true

  /**
   * Whether this serializer is a streamed serializer or not.
   *
   * @return Whether this is a streamed serializer.
   */
  def isStreamed: Boolean = false

  /**
   * Get a serializer for a client request.
   *
   * Since a client is the initiator of the request, it simply returns the default serializer for the entity.
   *
   * @return A serializer for request messages.
   */
  def serializerForRequest: CoreMessageSerializer.NegotiatedSerializer[MessageEntity, WireFormat]

  /**
   * Get a deserializer for an entity described by the given request or response protocol.
   *
   * @param protocol The protocol of the message request or response associated with teh entity.
   * @return A deserializer for request/response messages.
   * @throws UnsupportedMediaType If the deserializer can't deserialize that protocol.
   */
  @throws(classOf[UnsupportedMediaType])
  def deserializer(protocol: CoreMessageProtocol): CoreMessageSerializer.NegotiatedDeserializer[MessageEntity, WireFormat]

  /**
   * Negotiate a serializer for the response, given the accepted message headers.
   *
   * @param acceptedMessageProtocols The accepted message headers is a list of message headers that will be accepted by
   *                               the client. Any empty values in a message protocol, including the list itself,
   *                               indicate that any format is acceptable.
   * @throws NotAcceptable If the serializer can't meet the requirements of any of the accept headers.
   */
  @throws(classOf[NotAcceptable])
  def serializerForResponse(acceptedMessageProtocols: Seq[CoreMessageProtocol]): CoreMessageSerializer.NegotiatedSerializer[MessageEntity, WireFormat]

}

object CoreMessageSerializer {
  /**
   * A negotiated serializer.
   *
   * @param <MessageEntity> The type of entity that this serializer serializes.
   * @param <WireFormat> The wire format that this serializer serializes to.
   */
  abstract class NegotiatedSerializer[MessageEntity, WireFormat] {

    /**
     * Get the protocol associated with this entity.
     */
    def protocol: CoreMessageProtocol = CoreMessageProtocol(Option.empty, Option.empty, Option.empty)

    /**
     * Serialize the given message entity.
     *
     * @param messageEntity The entity to serialize.
     * @return The serialized entity.
     */
    @throws(classOf[SerializationException])
    def serialize(messageEntity: MessageEntity): WireFormat
  }

  /**
   * A negotiated deserializer.
   *
   * @param <MessageEntity> The type of entity that this serializer serializes.
   * @param <WireFormat> The wire format that this serializer serializes to.
   */
  abstract class NegotiatedDeserializer[MessageEntity, WireFormat] {

    /**
     * Deserialize the given wire format.
     *
     * @param wire The raw wire data.
     * @return The deserialized entity.
     */
    @throws(classOf[DeserializationException])
    def deserialize(wire: WireFormat): MessageEntity
  }
}