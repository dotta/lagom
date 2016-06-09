/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.broker.kafka

import java.util.Properties
import java.lang.reflect.Constructor

import scala.concurrent.duration.FiniteDuration

import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer
import org.slf4j.LoggerFactory

import com.lightbend.lagom.javadsl.api.broker.Topic.TopicId
import com.typesafe.config.Config

import javax.inject.Inject
import play.api.Configuration
import com.typesafe.config.ConfigException
import scala.reflect.ClassTag

trait ZooKeeperConfig {
  def hosts: String
  def sessionTimeout: FiniteDuration
  def connectionTimeout: FiniteDuration
  def isSecurityEnabled: Boolean
}

private[lagom] class ZooKeeperConfigImpl @Inject() (config: Configuration) extends ZooKeeperConfig {
  import scala.language.implicitConversions
  implicit def asFiniteDuration(d: java.time.Duration): FiniteDuration = scala.concurrent.duration.Duration.fromNanos(d.toNanos)

  override val hosts: String = config.underlying.getString("lagom.broker.kafka.zookeeper.hosts")
  override val sessionTimeout: FiniteDuration = config.underlying.getDuration("lagom.broker.kafka.zookeeper.client.session-timeout")
  override val connectionTimeout: FiniteDuration = config.underlying.getDuration("lagom.broker.kafka.zookeeper.client.connection-timeout")
  override val isSecurityEnabled: Boolean = config.underlying.getBoolean("lagom.broker.kafka.zookeeper.security-enabled")
}

trait KafkaConfig {
  def brokers: String
  def topicConfigOf(topicId: TopicId): TopicConfig
}

private[lagom] class KafkaConfigImpl @Inject() (config: Configuration) extends KafkaConfig {
  private val log = LoggerFactory.getLogger(classOf[KafkaConfigImpl])

  private val topicConfigKeyPrefix = "lagom.broker.kafka.topics"
  private def defaultConfigKey = topicConfigKeyPrefix + ".default"

  private val defaultConfig = config.underlying.getConfig(defaultConfigKey)

  override val brokers: String = config.underlying.getString("lagom.broker.kafka.brokers")

  override def topicConfigOf(topicId: TopicId): TopicConfig = {
    val topicConfigKey = s"$topicConfigKeyPrefix.${topicId.value}"
    val topicConfig =
      try {
        val c = config.underlying.getConfig(topicConfigKey).withFallback(defaultConfig)
        log.debug(s"Configuration for $topicId is created using the configuration under $topicConfigKey, with $defaultConfigKey as fallback.")
        c
      } catch {
        case e: ConfigException.Missing =>
          log.debug(s"No topic configuration found under key $topicConfigKey. Topic $topicId will be configured using $defaultConfigKey.")
          defaultConfig
      }

    TopicConfig(topicConfig)
  }
}

trait TopicConfig {
  def partitions: Int
  def replication: Int
  def properties: Properties
  def messageKeySerializer[T]: Serializer[T]
  def messageValueSerializer[T]: Serializer[T]
  def messageKeyDeserializer[T]: Deserializer[T]
  def messageValueDeserializer[T]: Deserializer[T]
}

object TopicConfig {
  private val log = LoggerFactory.getLogger(classOf[TopicConfig])
  private[lagom] def apply(topicConfig: Config): TopicConfig = new TopicConfigImpl(topicConfig)

  private[this] class TopicConfigImpl(config: Config) extends TopicConfig {
    override val partitions: Int = config.getInt("partitions")
    override val replication: Int = config.getInt("replication")
    override val properties: Properties = {
      import scala.collection.JavaConverters._
      val properties = new Properties()
      for {
        entry <- config.getConfig("properties").entrySet().asScala
      } properties.put(entry.getKey, entry.getValue)

      properties
    }

    override def messageKeySerializer[T]: Serializer[T] =
      createInstanceFromConfigKey[Serializer[T]]("serializer.key")

    override def messageValueSerializer[T]: Serializer[T] =
      createInstanceFromConfigKey[Serializer[T]]("serializer.value")

    override def messageKeyDeserializer[T]: Deserializer[T] =
      createInstanceFromConfigKey[Deserializer[T]]("deserializer.key")

    override def messageValueDeserializer[T]: Deserializer[T] =
      createInstanceFromConfigKey[Deserializer[T]]("deserializer.value")

    private def createInstanceFromConfigKey[T](configKey: String)(implicit t: ClassTag[T]): T = {
      var className: String = null
      var clazz: Class[_] = null
      var constructor: Constructor[_] = null
      var instance: Any = null
      try {
        className = config.getString(configKey)
        clazz = Class.forName(className)
        constructor = clazz.getConstructor()
        instance = constructor.newInstance()
        instance.asInstanceOf[T]
      } catch {
        case e: ClassNotFoundException =>
          log.error(s"Class $className not in the classpath.", e)
          throw new ConfigException.BadValue(config.origin, configKey, s"Class $className is not in the classpath, maybe you have misspelled it?")
        case e: NoSuchMethodException =>
          log.error(s"Failed instantiation because $className does not have a nullary constructor.", e)
          throw new ConfigException.BadValue(config.origin, configKey, s"Class $className does not have a nullary constructor.")
        case e: ClassCastException =>
          log.error(s"Failed to cast instance of $className to ${t.runtimeClass.getName}.", e)
          throw new ConfigException.BadValue(config.origin, configKey, s"Class $className is not a subtype of ${t.runtimeClass.getName}.", e)
        case t: Throwable =>
          log.error(s"Failed to create instance of $className.", t)
          throw new ConfigException.BadValue(config.origin, configKey, s"Failed to create instance of $className.", t)
      }
    }
  }
}
