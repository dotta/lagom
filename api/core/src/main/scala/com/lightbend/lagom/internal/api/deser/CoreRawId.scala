/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api.deser

import collection.immutable.Seq

/**
 * A raw ID.
 *
 * A raw ID consists of an ordered list of path parameters, which are ordered as they appear in the path, and a map of
 * query parameters.
 *
 * Raw ids are extracted fram the path according to a path spec.  For example, the following path spec:
 *
 * <pre>
 *     /blog/:blogId/post/:postId/comments?pageNo&amp;pageSize
 * </pre>
 *
 * With the following path:
 *
 * <pre>
 *     /blog/123/post/456/comments?pageNo=2
 * </pre>
 *
 * Will extract a raw ID with two path parameters, the first parameter being named <code>blogId</code> with a value of
 * <code>123</code>, the second value being named <code>postId</code> with a value of <code>456</code>.  It will also
 * have two query string parameters, one named <code>pageNo</code> with a value of <code>2</code>, the other named
 * <code>pageSize</code> with an empty value.
 *
 * Raw ids are what {@link IdSerializer} uses to convert the extracted path information into an Id type, and back.
 */

object CoreRawId {

  /**
   * An empty RawId.
   */
  val Empty: CoreRawId = new CoreRawId(Vector.empty, Map.empty)

  /**
   * A path parameter.
   *
   * A path parameter is an optional name, and a value.  The name is optional because it may not be known at
   * serialization time.  It is a hint that may be useful when deserializing, at which time the name will be the name
   * of the parameter in the path spec.  When serializing, the final name will be ignored - the order of the
   * parameters is used to format the path spec, not the names themselves.
   */
  case class PathParam private (value: String)(val name: Option[String]) {
    // Implementation note: Currying because `name` is not part of equality contract   
    override def toString: String = s"$name=$value"
  }
  object PathParam {
    def apply(name: Option[String], value: String): PathParam = new PathParam(value)(name)
    def apply(name: String, value: String): PathParam = new PathParam(value)(Option(name))
  }
}

case class CoreRawId(pathParams: Seq[CoreRawId.PathParam], queryParams: Map[String, Seq[String]]) {

  /**
   * Get the path parameter with the given <code>name</code>.
   *
   * @param name The name of the path parameter.
   * @return The path parameter, if it exists, otherwise empty.
   */
  def pathParam(name: String): Option[String] =
    pathParams.find(_.name == name).map(_.value)

  /**
   * Get a query string parameter with the given <code>name</code>.
   *
   * @param name The name of the query parameter.
   * @return The query string parameter, if it can be found, otherwise empty.
   */
  def queryParam(name: String): Option[String] =
    queryParams.get(name).map(_.headOption).flatten

  /**
   * Add the given query parameter.
   *
   * @param name The name of the query parameter.
   * @param values The values for the query parameter.
   * @return A new RawId with the added query parameter.
   */
  def withQueryParam(name: String, values: Seq[String]): CoreRawId =
    copy(queryParams = queryParams + (name -> values))

  /**
   * Add the given query parameter.
   *
   * @param name The name of the query parameter.
   * @param value The value for the query parameter.
   * @return A new RawId with the added query parameter.
   */
  def withQueryParam(name: String, value: Option[String]): CoreRawId =
    copy(queryParams = queryParams + (name -> value.toVector))

  /**
   * Add the given query parameter.
   *
   * @param name The name of the query parameter.
   * @param value The value of the query parameter.
   * @return A new RawId with the added query parameter.
   */
  def withQueryParam(name: String, value: String): CoreRawId =
    withQueryParam(name, Option(value))

  /**
   * Add the given path parameter.
   *
   * @param name The name of the path parameter.
   * @param value The value of the path parameter.
   * @return A new RawId with the added path parameter.
   */
  def withPathParam(name: String, value: String): CoreRawId =
    copy(pathParams :+ CoreRawId.PathParam(name, value))

  /**
   * Add the given path parameter value.
   *
   * @param value The value of the path parameter.
   * @return A new RawId with the added path parameter.
   */
  def withPathValue(value: String): CoreRawId =
    copy(pathParams :+ CoreRawId.PathParam(Option.empty, value))

  override def toString: String =
    s"RawId{pathParams=$pathParams, queryParams=$queryParams}"
}
