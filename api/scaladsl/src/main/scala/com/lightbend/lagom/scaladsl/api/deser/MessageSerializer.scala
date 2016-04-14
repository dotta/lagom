/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.scaladsl.api.deser

import scala.collection.immutable.Seq

import com.lightbend.lagom.api.deser.DeserializationException
import com.lightbend.lagom.api.deser.SerializationException
import com.lightbend.lagom.scaladsl.api.transport.MessageProtocol

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
class MessageSerializer[MessageEntity, WireFormat] {

  /**
   * The message headers that will be accepted for response serialization.
   */
  def acceptResponseProtocols: Seq[MessageProtocol] = Seq.empty

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
}

object MessageSerializer {
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
    def protocol: MessageProtocol = MessageProtocol()

    /**
     * Serialize the given message entity.
     *
     * @param messageEntity The entity to serialize.
     * @return The serialized entity.
     */
    @throws(classOf[SerializationException])
    def serialize(messageEntity: MessageEntity): WireFormat;
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