/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.javadsl.api.broker;

import java.lang.reflect.Type;

import com.lightbend.lagom.javadsl.api.broker.Topic.TopicId;

/**
 * Factory used for creating topic instances.
 *
 * @note This class is not meant to be used by users directly, hence it should
 * not be injected nor extended in user code.
 */
public interface Topics {
  <Message> Topic<Message> of(TopicId topicId, Type messageType);
}
