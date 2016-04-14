/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api.deser

import akka.util.ByteString

/**
 * A strict message serializer, for messages that fit and are worked with strictly in memory.
 *
 * Strict message serializers differ from streamed serializers, in that they work directly with `ByteString`, rather
 * than an Akka streams `Source`.
 */
trait CoreStrictMessageSerializer[MessageEntity] extends CoreMessageSerializer[MessageEntity, ByteString]
