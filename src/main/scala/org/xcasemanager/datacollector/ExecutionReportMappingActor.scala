package org.xcasemanager.datacollector

import akka.actor.Actor

/**
  * Actor taking care to verify and save case execution
  */
class ExecutionReportMappingActor extends Actor {

  /**
    * Path to the logActor
    */
  val logActor = context.actorSelection("/user/logActor")
  /**
    * Path to the reportActor
    */
  val reportActor = context.actorSelection("/user/reportActor")

  override def receive: Receive = {
    case msg : Envelope =>
      // Loading users from file
      val executions = CaseExecutionFileUtil.loadExecutionsFromFile()
      // If users contain the sender ID, we can proceed
      if(executions.contains(msg.senderId)){
        logActor ! "Message from "+msg.senderId+" accepted"
        // Respond a success message to the sender (async)
        sender() ! new OpSuccess("Operation accepted")
        // The message is passed to the senderActor (async)
        reportActor ! msg
      } else {
        // User is not among the registered users
        logActor ! "Message from "+msg.senderId+" refused"
        // Respond a failure message to the sender (async)
        sender() ! new OpFailure("Sender is not registered")
      }
  }
}