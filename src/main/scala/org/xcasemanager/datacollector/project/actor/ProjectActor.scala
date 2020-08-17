package org.xcasemanager.datacollector.project.actor

import akka.actor.{Actor, ActorLogging}
import org.xcasemanager.datacollector.domain.ProjectRequest
import org.xcasemanager.datacollector.service.ProjectService

class ProjectActor extends Actor with ActorLogging {
  private val projectService: ProjectService = new ProjectService()

  override def receive: Receive = {

    case SEARCH_ALL =>
      log.info(s"received message find all")
      sender() ! projectService.findAll

    case _ =>
      log.debug("Unhandled message!")
  }
}

sealed trait ProjectActorMessage

case object SEARCH_ALL extends ProjectActorMessage