/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api.transport

import java.nio.charset.Charset;
import java.util.Optional;

/**
 * Internal implementation of a message protocol.
 *
 * Each API provides its own implementation that delegates to this one.
 */
case class CoreMessageProtocol(contentType: Option[String], charset: Option[String], version: Option[String]) {

  def withContentType(contentType: String): CoreMessageProtocol = copy(contentType = Option(contentType))
  def withCharset(charset: String): CoreMessageProtocol = copy(charset = Option(charset))
  def withVersion(version: String): CoreMessageProtocol = copy(version = Option(version))

  def isText: Boolean = charset.isDefined
  def isUtf8: Boolean = charset match {
    case None          => false
    case Some(charset) => CoreMessageProtocol.Utf8Charset.equals(Charset.forName(charset))
  }

  def contentTypeHeader: Option[String] =
    contentType.map { ct => charset.fold(ct)(ct + "; charset=" + _) }
}

object CoreMessageProtocol {
  private val Utf8Charset: Charset = Charset.forName("utf-8")

  def of(contentType: Optional[String] = Optional.empty[String], charset: Optional[String] = Optional.empty[String], version: Optional[String] = Optional.empty[String]): CoreMessageProtocol = {
    import scala.compat.java8.OptionConverters._
    CoreMessageProtocol(contentType.asScala, charset.asScala, version.asScala)
  }

  def fromContentTypeHeader(contentType: Option[String]): CoreMessageProtocol = {
    contentType.map { ct =>
      val parts = ct.split(";")
      val justContentType = parts.headOption
      val charset = {
        val otherParts = parts.tail
        val charsetPart = otherParts.find { part =>
          val toTest = part.trim
          toTest.startsWith("charset=")
        }
        charsetPart.map(_.split("=", 2)(1))
      }
      CoreMessageProtocol(justContentType, charset, Option.empty)
    }.getOrElse(CoreMessageProtocol(Option.empty, Option.empty, Option.empty))
  }
}
