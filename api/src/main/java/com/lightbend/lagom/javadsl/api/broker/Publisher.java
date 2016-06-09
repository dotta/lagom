/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.javadsl.api.broker;

import akka.stream.javadsl.Source;
import akka.stream.javadsl.Flow;

public interface Publisher<Message> {
  void publish(Source<Message, ?> messages);
}
