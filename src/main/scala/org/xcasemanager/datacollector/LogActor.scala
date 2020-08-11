package org.xcasemanager.datacollector

import java.util.Date

import akka.actor.Actor

/**
  *  Logging actor
  */
class LogActor extends Actor {

  override def receive: Receive = {
    case msg : String =>
      println(new Date()+") "+msg)
  }
}
