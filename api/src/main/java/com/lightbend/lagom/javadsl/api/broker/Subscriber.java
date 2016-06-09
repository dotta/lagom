/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.javadsl.api.broker;

import java.util.Optional;

import akka.Done;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;

public interface Subscriber<Message> {

  public static interface Group {
    String groupId();
  }

  Subscriber<Message> withGroupId(String groupId);

  Optional<Group> group();

  Source<Message, ?> atMostOnceSource();

  Source<Done, ?> atLeastOnceSource(Flow<Message, Message, ?> flow);
}
