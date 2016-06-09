/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.kafka

object KafkaLauncher {
  def main(args: Array[String]): Unit = {
    val zookeperServerPort: Int =
      if (args.length > 0) args(0).toInt
      else Integer.getInteger("ZooKeeperServer.port", KafkaLocalServer.ZooKeperLocalServer.DefaultPort)

    val kafkaPropertiesFile: String =
      if (args.length > 1) args(1)
      else System.getProperty("Kafka.propertiesFile", KafkaLocalServer.DefaultPropertiesFile)

    val kafkaServer = KafkaLocalServer(kafkaPropertiesFile, zookeperServerPort)
    kafkaServer.start()
  }
}
