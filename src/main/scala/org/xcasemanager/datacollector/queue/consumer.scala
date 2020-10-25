package org.xcasemanager.datacollector.queue

import akka.Done
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.Sink
import akka.actor.{Actor, ActorLogging}
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}
import scala.language.postfixOps
import org.apache.kafka.common.serialization._
import org.xcasemanager.datacollector.queue.command.StartQueueConsumerCommand
import org.xcasemanager.datacollector.message._


/**
* Queue Consumer
*/
class Consumer extends Actor with JsonSupport with ActorLogging {

  implicit val system = context.system
  implicit val dispatcher: ExecutionContextExecutor = context.system.dispatcher

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
    val done = Consumer.committableSource(consumerSettings, Subscriptions.topics(topicName))
      .mapAsync(10) ( msg => {
         event(msg.record.key, msg.record.value).map(_ => msg.committableOffset)
    }).runWith(Sink.ignore)
  
    done onComplete {
      case Success(_) =>
        log.info(" ... successfully started)")
      case Failure(ex) =>
        log.info(s" ... Queue Receiver error: ${ex.getMessage}"); 
       
    }
  }

  /*
    event handler
  */
  def event(key: String, value: Array[Byte]): Future[Done] = {
    val promise = Promise[Int]
    promise.success(0)
    promise.future
  }
}