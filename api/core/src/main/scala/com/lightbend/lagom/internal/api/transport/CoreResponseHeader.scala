/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api.transport

import scala.collection.JavaConverters._
import scala.collection.immutable.Seq

import org.pcollections.PMap
import org.pcollections.PSequence
import com.lightbend.lagom.internal.converter.Collection

case class CoreResponseHeader private (
  status:                        Int,
  override val protocol:         CoreMessageProtocol,
  override val headers:          Map[String, Seq[String]],
  override val lowercaseHeaders: Map[String, Seq[String]]
) extends CoreMessageHeader(protocol, headers, lowercaseHeaders) {

  def withHeader(name: String, value: String): CoreResponseHeader = {
    val (newHeaders, newLowercaseHeaders) = computeNewHeaders(name, value)
    copy(headers = newHeaders, lowercaseHeaders = newLowercaseHeaders)
  }

  def withStatus(status: Int): CoreResponseHeader = copy(status = status)
  def withProtocol(protocol: CoreMessageProtocol): CoreResponseHeader = copy(protocol = protocol)

  def replaceAllHeaders(headers: Map[String, Seq[String]]): CoreResponseHeader = copy(headers = headers, lowercaseHeaders = CoreMessageHeader.toLowerCaseHeaders(headers))
  def replaceAllHeaders(headers: PMap[String, PSequence[String]]): CoreResponseHeader = replaceAllHeaders(Collection.asScala(headers))
}

object CoreResponseHeader {
  val Ok = CoreResponseHeader(200, CoreMessageProtocol(Option.empty, Option.empty, Option.empty), Map.empty)
  val NoContent = CoreResponseHeader(204, CoreMessageProtocol(Option.empty, Option.empty, Option.empty), Map.empty)

  def apply(status: Int, protocol: CoreMessageProtocol, headers: Map[String, Seq[String]]) =
    new CoreResponseHeader(status, protocol, headers, CoreMessageHeader.toLowerCaseHeaders(headers))

  def of(status: Int, protocol: CoreMessageProtocol, headers: PMap[String, PSequence[String]]) =
    apply(status, protocol, Collection.asScala(headers))

}