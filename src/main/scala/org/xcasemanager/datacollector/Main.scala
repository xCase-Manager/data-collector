package org.xcasemanager.datacollector

import akka.actor.{ActorSystem, Props}
import org.xcasemanager.datacollector.actors.project.ExecutionDataProcessActor
import org.xcasemanager.datacollector.actors.project.ExecutionRepoActor

object Main extends App {
    val actorSystem = ActorSystem.create("collector")
    val httpActor = actorSystem.actorOf(Props[HttpActor],"httpActor")
    val logActor = actorSystem.actorOf(Props[LogActor], "logActor")
    val executionDataProcessActor = actorSystem.actorOf(Props[ExecutionDataProcessActor], "executionDataProcessActor")
    val executionRepoActor = actorSystem.actorOf(Props[ExecutionRepoActor], "executionRepoActor")
    httpActor ! StartWebServerCommand
}
