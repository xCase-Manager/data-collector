package org.xcasemanager.datacollector

import akka.actor.Actor

/**
  * Actor handling the the report messages being pushed to the system
  */
class ReportActor extends Actor {

  override def receive: Receive = {
    case msg : Envelope =>
        CaseExecutionFileUtil.appendResultToFile(msg)
  }
}