/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api.deser

import com.lightbend.lagom.javadsl.api.deser.MessageSerializer
import scala.collection.immutable.Seq
import com.lightbend.lagom.internal.api.transport.CoreMessageProtocol
import com.lightbend.lagom.internal.converter.Collection
import com.lightbend.lagom.internal.api.transport.InternalMessageProtocol
import scala.collection.JavaConverters._
import com.lightbend.lagom.javadsl.api.transport.MessageProtocol
import com.lightbend.lagom.api.transport.UnsupportedMediaType
import com.lightbend.lagom.api.transport.NotAcceptable

class AdapterMessageSerializer[MessageEntity, WireFormat](val serializer: MessageSerializer[MessageEntity, WireFormat]) extends CoreMessageSerializer[MessageEntity, WireFormat] {

  override def acceptResponseProtocols: Seq[CoreMessageProtocol] =
    Collection.asScala(serializer.acceptResponseProtocols()).map(InternalMessageProtocol.obtainDelegate)

  override def isUsed: Boolean = serializer.isUsed

  override def isStreamed: Boolean = serializer.isStreamed

  override def serializerForRequest: CoreMessageSerializer.NegotiatedSerializer[MessageEntity, WireFormat] =
    ??? // serializer.serializerForRequest()

  /**
   * Get a deserializer for an entity described by the given request or response protocol.
   *
   * @param protocol The protocol of the message request or response associated with teh entity.
   * @return A deserializer for request/response messages.
   * @throws UnsupportedMediaType If the deserializer can't deserialize that protocol.
   */
  @throws(classOf[UnsupportedMediaType])
  override def deserializer(protocol: CoreMessageProtocol): CoreMessageSerializer.NegotiatedDeserializer[MessageEntity, WireFormat] =
    ??? // serializer.deserializer(new InternalMessageProtocol(protocol))

  /**
   * Negotiate a serializer for the response, given the accepted message headers.
   *
   * @param acceptedMessageProtocols The accepted message headers is a list of message headers that will be accepted by
   *                               the client. Any empty values in a message protocol, including the list itself,
   *                               indicate that any format is acceptable.
   * @throws NotAcceptable If the serializer can't meet the requirements of any of the accept headers.
   */
  @throws(classOf[NotAcceptable])
  def serializerForResponse(acceptedMessageProtocols: Seq[CoreMessageProtocol]): CoreMessageSerializer.NegotiatedSerializer[MessageEntity, WireFormat] =
    ??? // serializer.serializerForResponse(acceptedMessageProtocols.map(new InternalMessageProtocol(_): MessageProtocol).asJava)

}
