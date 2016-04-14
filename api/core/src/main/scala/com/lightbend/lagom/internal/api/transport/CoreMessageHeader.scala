/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api.transport

import java.util.Locale

import scala.collection.immutable.Seq

abstract class CoreMessageHeader protected (
  val protocol:         CoreMessageProtocol,
  val headers:          Map[String, Seq[String]],
  val lowercaseHeaders: Map[String, Seq[String]]
) {

  protected def this(protocol: CoreMessageProtocol, headers: Map[String, Seq[String]]) =
    this(protocol, headers, CoreMessageHeader.toLowerCaseHeaders(headers))

  def header(name: String): Option[String] = {
    val key = name.toLowerCase(Locale.ENGLISH)
    lowercaseHeaders.get(key).getOrElse(Seq.empty).headOption
  }

  protected def computeNewHeaders(name: String, value: String): (Map[String, Seq[String]], Map[String, Seq[String]]) = {
    val values = Seq(value)
    val newHeaders = headers + (name -> values)
    val newLowercaseHeaders = lowercaseHeaders + (name.toLowerCase(Locale.ENGLISH) -> values)
    (newHeaders, newLowercaseHeaders)
  }

}

object CoreMessageHeader {
  // this method is only used by CoreMessageHeader and its subclasses
  private[transport] def toLowerCaseHeaders(headers: Map[String, Seq[String]]): Map[String, Seq[String]] =
    for ((key, values) <- headers) yield key.toLowerCase(Locale.ENGLISH) -> values
}