package org.xcasemanager.datacollector

import akka.actor.{ActorSystem, Props}
import org.xcasemanager.datacollector.actors.project.ExecutionRepoActor
import org.xcasemanager.datacollector.queue.Publisher
import org.xcasemanager.datacollector.queue.Consumer
import org.xcasemanager.datacollector.web.WebService
import org.xcasemanager.datacollector.queue.StartQueueConsumerCommand
import org.xcasemanager.datacollector.web.command.StartWebServerCommand

object Main extends App {
    val actorSystem = ActorSystem.create("collector")
    actorSystem.actorOf(Props[ExecutionRepoActor], "executionRepoActor")
    actorSystem.actorOf(Props[Publisher], "publisher")
    
    val consumer = actorSystem.actorOf(Props[Consumer], "consumer")
    consumer ! StartQueueConsumerCommand

    val webService = actorSystem.actorOf(Props[WebService],"webService")
    webService ! StartWebServerCommand
}