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
import org.xcasemanager.datacollector.message.Project

/**
* Queue Publisher
*/
class Publisher extends Actor with ActorLogging {

  implicit val system = context.system
  val producerConfig = system.settings.config.getConfig("akka.kafka.producer")
  val bootstrapServers = system.settings.config.getString("queue.bootstrap.address")
  val topicName = system.settings.config.getString("queue.topic.name")
  val producerSettings =
    ProducerSettings(producerConfig, new StringSerializer, new StringSerializer)
    .withBootstrapServers(bootstrapServers)

  val done: Future[Done] =
    Source(1 to 100)
    .map(value => new ProducerRecord[String, String](topicName, "msg " + value))
    .runWith(Producer.plainSink(producerSettings))

  implicit val ec: ExecutionContextExecutor = system.dispatcher
  done onComplete  {
    case Success(_) => println("Done"); system.terminate()
    case Failure(err) => println(err.toString); system.terminate()
  }

  /*
    message handler
    @input message
  */
  def receive = {
    case projects: Seq[Project] =>
      Source(Seq[Project])
      .map(Project => new ProducerRecord[String, Project](topicName, Project))
      .runWith(Producer.plainSink(producerSettings))
  }
}