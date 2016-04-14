/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api.transport

import java.net.URI
import java.security.Principal
import java.util.Optional
import java.util.stream.{ Stream => JStream }

import scala.collection.JavaConverters._
import scala.collection.immutable.Seq
import scala.compat.java8.OptionConverters._
import scala.compat.java8.StreamConverters._

import org.pcollections.PMap
import org.pcollections.PSequence

import com.lightbend.lagom.api.transport.Method
import com.lightbend.lagom.internal.converter.Collection

case class CoreRequestHeader private (
  method:                        Method,
  uri:                           URI,
  override val protocol:         CoreMessageProtocol,
  acceptedResponseProtocols:     Seq[CoreMessageProtocol],
  principal:                     Option[Principal],
  override val headers:          Map[String, Seq[String]],
  override val lowercaseHeaders: Map[String, Seq[String]]
) extends CoreMessageHeader(protocol, headers, lowercaseHeaders) {

  def this(method: Method, uri: URI, protocol: CoreMessageProtocol, acceptedResponseProtocols: Seq[CoreMessageProtocol], principal: Option[Principal], headers: Map[String, Seq[String]]) =
    this(method, uri, protocol, acceptedResponseProtocols, principal, headers, CoreMessageHeader.toLowerCaseHeaders(headers))

  def withHeader(name: String, value: String): CoreRequestHeader = {
    val (newHeaders, newLowercaseHeaders) = computeNewHeaders(name, value)
    copy(headers = newHeaders, lowercaseHeaders = newLowercaseHeaders)
  }

  def withMethod(method: Method): CoreRequestHeader = copy(method = method)
  def withUri(uri: URI): CoreRequestHeader = copy(uri = uri)
  def withProtocol(protocol: CoreMessageProtocol): CoreRequestHeader = copy(protocol = protocol)
  def withAcceptedResponseProtocols(acceptedResponseProtocols: Seq[CoreMessageProtocol]): CoreRequestHeader = copy(acceptedResponseProtocols = acceptedResponseProtocols)
  def withAcceptedResponseProtocols(acceptedResponseProtocols: JStream[CoreMessageProtocol]): CoreRequestHeader = withAcceptedResponseProtocols(acceptedResponseProtocols.accumulate.toList)
  def withPrincipal(principal: Principal): CoreRequestHeader = copy(principal = Option(principal))
  def replaceAllHeaders(headers: Map[String, Seq[String]]): CoreRequestHeader = copy(headers = headers, lowercaseHeaders = CoreMessageHeader.toLowerCaseHeaders(headers))
  def replaceAllHeaders(headers: PMap[String, PSequence[String]]): CoreRequestHeader = replaceAllHeaders(Collection.asScala(headers))
}

object CoreRequestHeader {
  val Default = new CoreRequestHeader(Method.GET, URI.create("/"), new CoreMessageProtocol(Option.empty, Option.empty, Option.empty),
    Seq.empty, Option.empty, Map.empty)

  def of(method: Method, uri: URI, protocol: CoreMessageProtocol, acceptedResponseProtocols: JStream[CoreMessageProtocol],
         principal: Optional[Principal], headers: PMap[String, PSequence[String]]): CoreRequestHeader = {
    new CoreRequestHeader(method, uri, protocol, acceptedResponseProtocols.toScala(Seq.canBuildFrom), principal.asScala, Collection.asScala(headers))
  }
}