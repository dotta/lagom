/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */

package com.lightbend.lagom.javadsl.broker.kafka.message;

public interface HasPartition {
  int partition();
}
