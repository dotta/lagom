package com.lightbend.lagom.javadsl.broker.kafka.message;

public interface HasKey<T> {
  T key();
}
