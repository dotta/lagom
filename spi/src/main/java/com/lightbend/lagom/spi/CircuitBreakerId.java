/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.spi;

public class CircuitBreakerId {
  private final String id;

  public CircuitBreakerId(String id) {
      this.id = id;
  }

  /**
   * Get the identifier of the circuit breaker
   */
  public String id() {
      return id;
  }

  @Override
  public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof CircuitBreakerId)) return false;

      CircuitBreakerId that = (CircuitBreakerId) o;
      return id.equals(that.id);
  }

  @Override
  public int hashCode() {
      return id.hashCode();
  }

  @Override
  public String toString() {
      return "CircuitBreakerId{" +
              "id='" + id + '\'' +
              '}';
  }
}