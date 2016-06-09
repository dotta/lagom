package com.lightbend.lagom.javadsl.broker.kafka.message;

@FunctionalInterface
public interface PartitioningStrategy {
  Integer partitionOf(Object message);
}
