package org.xcasemanager.datacollector.queue

import akka.Done
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}
import akka.actor.{Actor, ActorLogging}

/**
* Queue Publisher
*/
class Publisher extends Actor with ActorLogging {

    implicit val system = context.system
    val producerConfig = system.settings.config.getString("akka.kafka.producer")
    val bootstrapServers = system.settings.config.getString("queue.bootstrap.address")
    val topicName = system.settings.config.getString("queue.topic.name")
    val producerSettings =
        ProducerSettings(producerConfig, new StringSerializer, new StringSerializer)
    .withBootstrapServers(bootstrapServers)

}