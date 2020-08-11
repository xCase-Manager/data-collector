package org.xcasemanager.datacollector

import akka.actor.Actor

/**
  * Actor taking care of registering an execution
  */
class CaseExecutionRegisterActor extends Actor {

  /**
    * The path to the log actor
    */
  val logActor = context.actorSelection("/user/logActor")

  override def receive: Receive = {
    case msg : Registration =>
      logActor ! "Registering execution "+msg.id

      // Loading the current users from file
      val data = CaseExecutionFileUtil.loadExecutionsFromFile()
      // Setting the new user and saving the whole collection to file
      CaseExecutionFileUtil.saveCaseExectionToFile(data + (msg.id -> msg))
  }
}