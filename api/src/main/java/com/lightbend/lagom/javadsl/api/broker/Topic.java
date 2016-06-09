/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.javadsl.api.broker;

/**
 * FIXME: Document...
 * 
 * @note This class is not meant to be extended by client code.
 * @param <Message>
 */
public interface Topic<Message> {

  TopicId topicId();

  Subscriber<Message> subscribe();
  
  Publisher<Message> publisher();

  public static final class TopicId {

    private final String value;

    private TopicId(String value) {
      this.value = value;
    }

    public static TopicId of(String topicId) {
      return new TopicId(topicId);
    }

    public String value() {
      return value;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((value == null) ? 0 : value.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      TopicId other = (TopicId) obj;
      if (value == null) {
        if (other.value != null)
          return false;
      } else if (!value.equals(other.value))
        return false;
      return true;
    }

    @Override
    public String toString() {
      return "{ topicId = " + value + " }";
    }
  }

}
