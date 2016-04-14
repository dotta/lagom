/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.scaladsl.api.deser

import com.lightbend.lagom.scaladsl.api.paging.Page

import akka.NotUsed

/**
 * An ID serializer is responsible for serializing and deserializing IDs from URL paths.
 *
 * A path is specified in the framework using a String like <code>/blog/:blogId/post/:postId</code>.
 * The <code>blogId</code> and <code>postId</code> parts of the path are extracted, and passed in order in a list of
 * parameters to the IdSerializer.  When creating a path, the IdSerializer takes the id object, and turns it back into
 * a list of path parameters.
 *
 * @param <Id> The type of the ID.
 */
trait IdSerializer[Id] {

  /**
   * Serialize the given <code>id</code> into a list of path parameters.
   *
   * @param id The id to serialize.
   * @return The RawId.
   */
  def serialize(id: Id): RawId

  /**
   * Deserialize the <code>rawId</code> into an ID.
   *
   * @return The deserialized ID.
   */
  def deserialize(rawId: RawId): Id

  /**
   * A hint of the number of path parameters that this id serializer serializes.
   *
   * This is used by id serializers that compose with other ID serializers, so that a parent serializer can know
   * how many parameters its children extract, that way it can modify the raw ID it passes to them in order for them
   * to deserialize just the part of the ID that they are interested in.
   *
   * If this returns empty, it means this ID serializer cannot be composed with other IdSerializers that extract
   * path parameters.
   *
   * @return The number of path parameters that this ID serializer extracts.
   */
  def numPathParamsHint: Option[Int]
}

object IdSerializer {
  /**
   * Create an IdSerializer from the given serialize and deserialize functions.
   *
   * This is useful for situations where the ID is expressed as a single parameter, and the name of that parameter
   * doesn't matter.
   *
   * @param name The name of the serializer. This is used for debugging purposes, so that the toString method returns
   *             something meaningful.
   * @param deserializer The deserialize function.
   * @param serializer The serialize function.
   * @param <Id> The type of the Id that this serializer is for.
   * @return The serializer.
   * @see IdSerializer
   */
  def createSinglePathParam[Id](name: String, deserializer: String => Id, serializer: Id => String): IdSerializer[Id] =
    NamedIdSerializer[Id](name, Some(1)) { id =>
      val param = RawId.PathParam(Option.empty[String], serializer(id))
      RawId(Vector(param), Map.empty)
    } { rawId =>
      if (rawId.pathParams.isEmpty)
        throw new IllegalArgumentException(name + " can't parse empty path")
      else
        deserializer(rawId.pathParams.head.value)
    }

  /**
   * A String id serializer.
   */
  implicit val stringIdSerializer: IdSerializer[String] = createSinglePathParam("String", identity, identity)

  /**
   * A Long id serializer.
   */
  implicit val longIdSerializer: IdSerializer[Long] = createSinglePathParam("Long", _.toLong, _.toString)

  /**
   * An Integer id serializer.
   */
  implicit val intIdSerializer: IdSerializer[Int] = createSinglePathParam("Int", _.toInt, _.toString)

  /**
   * A page serializer.
   */
  implicit val pageIdSerializer: IdSerializer[Page] =
    NamedIdSerializer[Page]("Page") { page =>
      RawId.Empty.withQueryParam("pageNo", page.pageNo.map(_.toString))
        .withQueryParam("pageSize", page.pageSize.map(_.toString()))
    } { rawId =>
      val pageNo = rawId.queryParam("pageNo").map(_.toInt)
      val pageSize = rawId.queryParam("pageSize").map(_.toInt)
      Page(pageNo, pageSize)
    }

  /**
   * A unit id serializer.
   */
  implicit val notUsedIdSerializer: IdSerializer[NotUsed] =
    NamedIdSerializer[NotUsed]("NotUsed") { notUsed => RawId.Empty } { rawId => NotUsed }

  private abstract class NamedIdSerializer[Id](name: String) extends IdSerializer[Id] {
    override def toString: String = "IdSerializer(" + name + ")"
  }

  private object NamedIdSerializer {
    def apply[Id](name: String, _numPathParamsHint: Option[Int] = Option.empty)(serializer: Id => RawId)(deserializer: RawId => Id): NamedIdSerializer[Id] =
      new NamedIdSerializer[Id](name) {
        override def serialize(id: Id): RawId = serializer(id)
        override def deserialize(rawId: RawId): Id = deserializer(rawId)
        override def numPathParamsHint: Option[Int] = _numPathParamsHint
      }
  }
}
