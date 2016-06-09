/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.kafka

import java.util.Properties

import org.I0Itec.zkclient.ZkClient
import org.I0Itec.zkclient.ZkConnection
import org.scalatest.Finders
import org.scalatest.Matchers
import org.scalatest.WordSpecLike

import com.lightbend.lagom.util.PropertiesLoader

import kafka.admin.AdminUtils
import kafka.javaapi.producer.Producer
import kafka.producer.ProducerConfig
import kafka.utils.ZkUtils

class KafkaLocalServerSpec extends WordSpecLike with Matchers {

  // FIXME: May be too costly to create a fresh Kafka instance for each test
  "A local Kafka server" can {
    "be started" in withKafkaServer { kafka =>
      val props = PropertiesLoader.from("/kafka-producer.properties")
      val producer = new Producer[String, String](new ProducerConfig(props));
      producer.close
    }
    "allow creating a topic" in withKafkaServer { kafka =>
      var zkClient: ZkClient = null
      try {
        val zookeeperHosts = s"localhost:${KafkaLocalServer.ZooKeperLocalServer.DefaultPort}"
        val sessionTimeOutInMs = 10 * 1000; // 15 secs
        val connectionTimeOutInMs = 10 * 1000; // 10 secs
        zkClient = new ZkClient(zookeeperHosts, sessionTimeOutInMs, connectionTimeOutInMs)
        val zkUtils = new ZkUtils(zkClient, new ZkConnection(zookeeperHosts), false)
        val topicName = "testTopic"
        val noOfPartitions = 2
        val noOfReplication = 1
        val topicConfiguration = new Properties()
        AdminUtils.createTopic(zkUtils, topicName, noOfPartitions, noOfReplication, topicConfiguration)
        assert(AdminUtils.topicExists(zkUtils, topicName), s"Expected topic $topicName to exist")
      } finally if (zkClient != null) zkClient.close()
    }
  }

  def withKafkaServer[T](body: KafkaLocalServer => T): T = {
    val kafka = KafkaLocalServer()
    try {
      kafka.start()
      body(kafka)
    } finally kafka.stop()
  }
}
