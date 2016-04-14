/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api.transport

import java.net.URI
import java.net.URISyntaxException
import java.util.regex.Pattern

import scala.collection.immutable.Seq

class CorePathVersionedProtocolNegotiationStrategy(pathVersionExtractor: Pattern, pathVersionFormat: String) extends CoreHeaderTransformer {

  override def transformClientRequest(request: CoreRequestHeader): CoreRequestHeader = request.protocol.version match {
    case None => request
    case Some(version) =>
      // Just read the version from the request protocol
      val uri = request.uri
      val path = String.format(pathVersionFormat, version, uri.getPath())
      try {
        val newUri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), path,
          uri.getQuery(), uri.getFragment())
        request.copy(uri = newUri)
      } catch {
        case e: URISyntaxException => throw new RuntimeException(e);
      }

  }

  override def transformServerRequest(request: CoreRequestHeader): CoreRequestHeader = {
    val uri = request.uri
    val matcher = pathVersionExtractor.matcher(uri.getPath());
    if (matcher.matches()) {
      val version = matcher.group(1)
      val remainder = matcher.group(2)
      try {
        val newUri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), remainder,
          uri.getQuery(), uri.getFragment())

        val acceptedResponseProtocols = {
          if (request.acceptedResponseProtocols.isEmpty)
            Seq(CoreMessageProtocol(Option.empty, Option.empty, Option(version)))
          else
            request.acceptedResponseProtocols.map(_.withVersion(version))
        }
        request.copy(uri = newUri, protocol = request.protocol.withVersion(version), acceptedResponseProtocols = acceptedResponseProtocols)
      } catch {
        case e: URISyntaxException => throw new RuntimeException(e);
      }
    } else request
  }

  override def transformServerResponse(response: CoreResponseHeader, request: CoreRequestHeader): CoreResponseHeader = response

  override def transformClientResponse(response: CoreResponseHeader, request: CoreRequestHeader): CoreResponseHeader = request.protocol.version match {
    case None => response
    case Some(version) =>
      val newProtocol = response.protocol.copy(version = Option(version))
      response.copy(protocol = newProtocol)
  }

}

object CorePathVersionedProtocolNegotiationStrategy {
  private[this] val DefaultPathVersionExtractor = Pattern.compile("/(^/+)(/.*)")

  def apply(): CorePathVersionedProtocolNegotiationStrategy = new CorePathVersionedProtocolNegotiationStrategy(DefaultPathVersionExtractor, "/%s%s")
}