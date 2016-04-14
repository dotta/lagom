/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.scaladsl.api.deser

import scala.reflect.ClassTag

trait SerializerFactory {

  def messageSerializerFor[MessageEntity, T: ClassTag](): MessageSerializer[MessageEntity, _]

}