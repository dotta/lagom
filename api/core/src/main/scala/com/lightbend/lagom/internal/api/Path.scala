/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api

import java.util.regex.Pattern

import scala.collection.immutable.Seq
import scala.util.parsing.combinator.JavaTokenParsers

import com.lightbend.lagom.internal.api.CoreDescriptor.CallId
import com.lightbend.lagom.internal.api.CoreDescriptor.NamedCallId
import com.lightbend.lagom.internal.api.CoreDescriptor.PathCallId
import com.lightbend.lagom.internal.api.CoreDescriptor.RestCallId
import com.lightbend.lagom.internal.api.deser.CoreRawId
import com.lightbend.lagom.internal.api.deser.CoreRawId.PathParam

import akka.util.ByteString
import play.utils.UriEncoding

case class Path(parts: Seq[PathPart], queryParams: Seq[String]) {
  val regex = parts.map(_.expression).mkString.r
  private val dynamicParts = parts.collect {
    case dyn: DynamicPathPart => dyn
  }

  def extract(path: String, query: Map[String, Seq[String]]): Option[CoreRawId] = {
    regex.unapplySeq(path).map { partValues =>
      val pathParams = dynamicParts.zip(partValues).map {
        case (part, value) =>
          val decoded = if (part.encoded) {
            UriEncoding.decodePathSegment(value, ByteString.UTF_8)
          } else value
          PathParam(part.name, decoded)
      }
      CoreRawId(pathParams, query)
    }
  }

  def format(rawId: CoreRawId): (String, Map[String, Seq[String]]) = {
    val (resultPathParts, _) = parts.foldLeft((Seq.empty[String], rawId.pathParams)) {
      case ((pathParts, params), StaticPathPart(path)) => (pathParts :+ path, params)
      case ((pathParts, params), DynamicPathPart(name, _, encoded)) =>
        val encodedValue = params.headOption match {
          case Some(value) =>
            if (encoded) UriEncoding.encodePathSegment(value.value, ByteString.UTF_8)
            else value.value
          case None => throw new IllegalArgumentException("RawId does not contain required path param name: " + name)
        }
        (pathParts :+ encodedValue, params.tail)
    }
    val path = resultPathParts.mkString
    val queryParams = for ((name, values) <- rawId.queryParams if values.nonEmpty) yield name -> values
    path -> queryParams
  }

}

sealed trait PathPart {
  def expression: String
}

case class DynamicPathPart(name: String, regex: String, encoded: Boolean) extends PathPart {
  def expression = "(" + regex + ")"
}

case class StaticPathPart(path: String) extends PathPart {
  def expression = Pattern.quote(path)
}
object Path {
  private object PathSpecParser extends JavaTokenParsers {
    def namedError[A](p: Parser[A], msg: String): Parser[A] = Parser[A] { i =>
      p(i) match {
        case Failure(_, in) => Failure(msg, in)
        case o              => o
      }
    }

    val identifier = namedError(ident, "Identifier expected")

    def singleComponentPathPart: Parser[DynamicPathPart] = (":" ~> identifier) ^^ {
      case name => DynamicPathPart(name, """[^/]+""", encoded = true)
    }

    def multipleComponentsPathPart: Parser[DynamicPathPart] = ("*" ~> identifier) ^^ {
      case name => DynamicPathPart(name, """.+""", encoded = false)
    }

    def regexSpecification: Parser[String] = "<" ~> """[^>\s]+""".r <~ ">"

    def regexComponentPathPart: Parser[DynamicPathPart] = "$" ~> identifier ~ regexSpecification ^^ {
      case name ~ regex => DynamicPathPart(name, regex, encoded = false)
    }

    def staticPathPart: Parser[StaticPathPart] = """[^:\*\$\?\s]+""".r ^^ {
      case path => StaticPathPart(path)
    }

    def queryParam: Parser[String] = namedError("""[^&]+""".r, "Query parameter name expected")

    def queryParams: Parser[Seq[String]] = "?" ~> repsep(queryParam, "&")

    def pathSpec: Parser[Seq[PathPart]] = "/" ~> (staticPathPart | singleComponentPathPart | multipleComponentsPathPart | regexComponentPathPart).* ^^ {
      case parts => parts match {
        case StaticPathPart(path) :: tail => StaticPathPart(s"/$path") :: tail
        case _                            => StaticPathPart("/") :: parts
      }
    }

    def parser: Parser[Path] = pathSpec ~ queryParams.? ^^ {
      case parts ~ queryParams => Path(parts, queryParams.getOrElse(Nil))
    }

  }

  def fromCallId(callId: CallId): Path = {
    callId match {
      case rest: RestCallId =>
        Path.parse(rest.pathPattern)
      case path: PathCallId =>
        Path.parse(path.pathPattern)
      case named: NamedCallId =>
        val name = named.name
        val path = if (name.startsWith("/")) name else "/" + name
        Path(Seq(StaticPathPart(path)), Nil)
    }
  }

  def parse(spec: String): Path = {
    PathSpecParser.parseAll(PathSpecParser.parser, spec) match {
      case PathSpecParser.Success(path, _)  => path
      case PathSpecParser.NoSuccess(msg, _) => throw new IllegalArgumentException(s"Error parsing $spec: $msg")
    }
  }
}
