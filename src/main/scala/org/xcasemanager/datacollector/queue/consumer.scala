package org.xcasemanager.datacollector.queue

import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.actor.{Actor, ActorLogging}
import scala.concurrent.Future
import scala.language.postfixOps
import org.apache.kafka.common.serialization.{StringSerializer, ByteArraySerializer}
import org.xcasemanager.datacollector.queue.command.StartConsummerCommand

/**
* Queue Consumer
*/
class Consumer extends Actor with JsonSupport with ActorLogging {

  implicit val system = context.system
  val config = system.settings.config.getConfig("akka.kafka.consumer")
  val topicName = system.settings.config.getString("queue.topic.name")
 
  /*
    message handler
    @input message
  */
  def receive = {
    case StartConsummerCommand =>
      startConsumer
  }

  /*
    listen to topic
  */
  def startConsumer = {
    Consumer.plainSource(config, Subscriptions.topics(topicName))
      .mapAsync(1) ( msg => {
        log.info(s"Message Received : ${msg.timestamp} - ${msg.value}")
      Future.successful(msg)
    }).runWith(Sink.ignore)
  }
}