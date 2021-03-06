package org.xcasemanager.datacollector.queue

import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import akka.actor.{Actor, ActorLogging}
import akka.serialization._
import org.xcasemanager.datacollector.message._

/**
* Queue Publisher
*/
class Publisher extends Actor with JsonSupport with ActorLogging {

  implicit val system = context.system
  val topicName = system.settings.config.getString("queue.topic.name")
  val producerSettings =
    ProducerSettings(system, new StringSerializer, new ByteArraySerializer)
  val serialization = SerializationExtension(system)

  /*
    message handler
    @input message
  */
  def receive = {
    case projects: Seq[Project] =>
      Source(projects)
      .map(Project => new ProducerRecord[String, Array[Byte]](topicName, 
        serialization.serialize(Project).get))
      .runWith(Producer.plainSink(producerSettings))
  }
}