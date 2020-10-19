package org.xcasemanager.datacollector.queue

import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.Sink
import akka.actor.{Actor, ActorLogging}
import scala.concurrent.Future
import scala.language.postfixOps
import org.apache.kafka.common.serialization._
import org.xcasemanager.datacollector.queue.command.StartQueueConsumerCommand
import org.xcasemanager.datacollector.message._


/**
* Queue Consumer
*/
class Consumer extends Actor with JsonSupport with ActorLogging {

  implicit val system = context.system

  val config = system.settings.config.getConfig("akka.kafka.consumer")
  val consumerSettings =
    ConsumerSettings(config, new StringDeserializer, new ByteArrayDeserializer)
  val topicName = system.settings.config.getString("queue.topic.name")
 
  /*
    message handler
    @input message
  */
  def receive = {
    case StartQueueConsumerCommand =>
      startConsumer
  }

  /*
    listen to topic
  */
  def startConsumer = {
    Consumer.plainSource(consumerSettings, Subscriptions.topics(topicName))
      .mapAsync(1) ( msg => {
        log.info(s"Message Received : ${msg.timestamp} - ${msg.value}")
      Future.successful(msg)
    }).runWith(Sink.ignore)
  }
}