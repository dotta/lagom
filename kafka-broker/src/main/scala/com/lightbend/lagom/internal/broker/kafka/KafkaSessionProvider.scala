/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.broker.kafka

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import org.slf4j.LoggerFactory

import com.lightbend.lagom.internal.broker.SessionProvider
import com.lightbend.lagom.internal.jackson.JacksonObjectMapperProvider
import com.lightbend.lagom.javadsl.api.broker.Topic.TopicId

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.ActorMaterializerSettings
import javax.inject.Inject
import kafka.admin.AdminUtils
import kafka.utils.ZkUtils

class KafkaSessionProvider @Inject() (zooKeeperConfig: ZooKeeperConfig, kafkaConfig: KafkaConfig, system: ActorSystem)(implicit ec: ExecutionContext) extends SessionProvider {
  private val log = LoggerFactory.getLogger(classOf[KafkaSessionProvider])

  override def createSession(): KafkaSessionProvider.Session = {
    val sessionTimeoutMs = zooKeeperConfig.sessionTimeout.toMillis.toInt
    val connectionTimeoutMs = zooKeeperConfig.connectionTimeout.toMillis.toInt
    val zkClient = ZkUtils.createZkClient(zooKeeperConfig.hosts, sessionTimeoutMs, connectionTimeoutMs)
    val zkUtils = ZkUtils(zkClient, zooKeeperConfig.isSecurityEnabled)

    log.debug {
      s"Created Zookeeper client [sessionTimeoutMs=$sessionTimeoutMs] " +
        s"[connectionTimeoutMs=$connectionTimeoutMs] [isZkSecurityEnabled=${zooKeeperConfig.isSecurityEnabled}]"
    }

    val session = new KafkaSessionProvider.Session(kafkaConfig, zkUtils, system)
    log.debug(s"Created Kafka Zookeeper session (instance identity = $session).")
    session
  }
}

object KafkaSessionProvider {
  class Session(kafkaConfig: KafkaConfig, zkUtils: ZkUtils, system: ActorSystem)(implicit ec: ExecutionContext) extends SessionProvider.Session {
    private val log = LoggerFactory.getLogger(classOf[Session])

    private val objectMapperProvider = JacksonObjectMapperProvider.get(system)

    private implicit val mat = {
      val settings = ActorMaterializerSettings(system)
      ActorMaterializer(settings)(system)
    }

    override def newTopic(topicId: TopicId): Future[Done] = Future {
      if (!AdminUtils.topicExists(zkUtils, topicId.value)) {
        val topicConfig = kafkaConfig.topicConfigOf(topicId)

        AdminUtils.createTopic(zkUtils, topicId.value, topicConfig.partitions, topicConfig.replication, topicConfig.properties)

        if (log.isDebugEnabled()) {
          import scala.collection.JavaConverters._
          val propertiesAsText = for {
            entry <- topicConfig.properties.entrySet().asScala
          } yield s"[${entry.getKey}=${entry.getValue}]"
          log.info(s"Created Kafka topic $topicId [partitions=${topicConfig.partitions}] [replication=${topicConfig.replication}] with properties ${propertiesAsText.mkString(" ")}")
        }
      } else log.debug(s"Request of creating a Kafka topic $topicId was ignored because it already exists.")
      Done
    }

    override def deleteTopic(topicId: TopicId): Future[Done] = Future {
      AdminUtils.deleteTopic(zkUtils, topicId.value)
      Done
    }

    override def close(): Unit = {
      log.debug(s"Closing Kafka Zookeeper session (instance identity = $this).")
      zkUtils.close()
    }
  }
}
