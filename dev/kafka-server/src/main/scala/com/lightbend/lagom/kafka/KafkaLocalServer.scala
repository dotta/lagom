/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.kafka

import java.util.Properties
import java.util.concurrent.atomic.AtomicReference

import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.RetryUntilElapsed
import org.apache.curator.test.TestingServer

import com.lightbend.lagom.kafka.KafkaLocalServer.ZooKeperLocalServer
import com.lightbend.lagom.util.PropertiesLoader

import kafka.server.KafkaConfig
import kafka.server.KafkaServerStartable

class KafkaLocalServer private (kafkaProperties: Properties, zooKeeperServer: ZooKeperLocalServer) {

  private val kafkaServerRef = new AtomicReference[KafkaServerStartable](null)

  def start(): Unit = {
    // FIXME: This leaks kafka server instances if NOT null!
    if (kafkaServerRef.compareAndSet(null, KafkaServerStartable.fromProps(kafkaProperties))) {
      zooKeeperServer.start()
      val kafkaServer = kafkaServerRef.get
      kafkaServer.startup()
    }
    // else it's already running
  }

  def stop(): Unit = {
    val kafkaServer = kafkaServerRef.getAndSet(null)
    if (kafkaServer != null) {
      kafkaServer.shutdown()
      zooKeeperServer.stop()
    }
    // else it's already stopped
  }

}

object KafkaLocalServer {
  final val DefaultPropertiesFile = "/kafka-server.properties"

  def apply(): KafkaLocalServer = this(DefaultPropertiesFile, ZooKeperLocalServer.DefaultPort)

  def apply(kafkaPropertiesFile: String, zooKeperServerPort: Int): KafkaLocalServer = {
    val kafkaProperties = PropertiesLoader.from(kafkaPropertiesFile)
    new KafkaLocalServer(kafkaProperties, new ZooKeperLocalServer(zooKeperServerPort))
  }

  private class ZooKeperLocalServer(port: Int) {
    private val zooKeeperServerRef = new AtomicReference[TestingServer](null)

    def start(): Unit = {
      if (zooKeeperServerRef.compareAndSet(null, new TestingServer(port, /*start=*/ false))) {
        val zooKeeperServer = zooKeeperServerRef.get
        zooKeeperServer.start()
        val client = CuratorFrameworkFactory.newClient(zooKeeperServer.getConnectString(), new RetryUntilElapsed(3000, 500))
        try {
          client.start()
          client.blockUntilConnected()
        } finally client.close()
      }
      // else it's already running
    }

    def stop(): Unit = {
      val zooKeeperServer = zooKeeperServerRef.getAndSet(null)
      if (zooKeeperServer != null)
        zooKeeperServer.stop()
      // else it's already stopped
    }
  }

  object ZooKeperLocalServer {
    final val DefaultPort = 2181
  }
}
