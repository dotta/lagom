/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.api.deser;

import java.io.Serializable;

/**
 * A high level exception message.
 *
 * This is used by the default exception serializer to serialize exceptions into
 * messages.
 */
// FIXME: Why implementing Serializable?
public class ExceptionMessage implements Serializable {
  private final String name;
  private final String detail;

  public ExceptionMessage(String name, String detail) {
    this.name = name;
    this.detail = detail;
  }

  public String name() {
    return name;
  }

  public String detail() {
    return detail;
  }

  @Override
  public String toString() {
    return "ExceptionMessage{" + "name='" + name + '\'' + ", detail='" + detail + '\'' + '}';
  }
}
