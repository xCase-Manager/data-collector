package org.xcasemanager.datacollector

import akka.actor.{ActorSystem, Props}
import org.xcasemanager.datacollector.repository.ExecutionRepository
import org.xcasemanager.datacollector.queue.Publisher
import org.xcasemanager.datacollector.queue.Consumer
import org.xcasemanager.datacollector.web.WebService
import org.xcasemanager.datacollector.queue.command.StartQueueConsumerCommand
import org.xcasemanager.datacollector.web.command.StartWebServerCommand

object Main extends App {
    val actorSystem = ActorSystem.create("collector")
    actorSystem.actorOf(Props[ExecutionRepository], "executionRepository")
    actorSystem.actorOf(Props[Publisher], "publisher")
    
    val consumer = actorSystem.actorOf(Props[Consumer], "consumer")
    consumer ! StartQueueConsumerCommand

    val webService = actorSystem.actorOf(Props[WebService],"webService")
    webService ! StartWebServerCommand
}