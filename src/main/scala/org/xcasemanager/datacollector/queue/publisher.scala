package org.xcasemanager.datacollector.queue

import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import akka.actor.{Actor, ActorLogging}
import akka.serialization._
import org.xcasemanager.datacollector.message._

/**
* Queue Publisher
*/
class Publisher extends Actor with JsonSupport with ActorLogging {

  implicit val system = context.system
  val config = system.settings.config.getConfig("akka.kafka.producer")
  val topicName = system.settings.config.getString("queue.topic.name")
  val producerSettings =
    ProducerSettings(config, new StringSerializer, new ByteArraySerializer)
    .withBootstrapServers(bootstrapServers)

  /*
    message handler
    @input message
  */
  def receive = {
    case projects: Seq[Project] =>
      Source(projects)
      .map(Project => new ProducerRecord[String, ByteArray](topicName, 
        serialization.serialize(Project).get))
      .runWith(Producer.plainSink(producerSettings))
  }
}