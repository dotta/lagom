/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api.deser

import akka.stream.javadsl.Source
import akka.util.ByteString

/**
 * A streamed message serializer, for streams of messages.
 */
trait CoreStreamedMessageSerializer[MessageEntity] extends CoreMessageSerializer[Source[MessageEntity, _], Source[ByteString, _]] {
  override final def isStreamed: Boolean = true
}