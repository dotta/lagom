/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.converter

import org.pcollections.HashTreePMap
import org.pcollections.PMap
import org.pcollections.PSequence
import org.pcollections.TreePVector

import scala.collection.JavaConverters._
import scala.compat.java8.FunctionConverters._
import scala.collection.immutable.Seq

import java.util.function.{ Function => JFunction }

object Collection {
  def asPCollection[A](original: Seq[A]): PSequence[A] =
    TreePVector.from(original.asJava)

  def asScala[A](original: PSequence[A]): Seq[A] =
    original.asScala.toList

  def asPCollection[K, V](original: Map[K, Seq[V]]): PMap[K, PSequence[V]] = {
    original.foldLeft(HashTreePMap.empty[K, PSequence[V]]) { (acc, entry) =>
      val (key, values) = entry
      acc.plus(key, TreePVector.from(values.asJava))
    }
  }

  def asScala[K, V](original: PMap[K, PSequence[V]]): Map[K, Seq[V]] = {
    (for {
      (key, values) <- original.asScala
    } yield key -> values.asScala.toVector)(collection.breakOut)
  }

  def map[A, B](original: Seq[A], f: JFunction[A, B]): PSequence[B] =
    TreePVector.from(original.map(f.asScala).asJava)

  def map[A, B](original: PSequence[A], f: JFunction[A, B]): Seq[B] =
    (original.asScala.map(f.asScala))(collection.breakOut)

}
