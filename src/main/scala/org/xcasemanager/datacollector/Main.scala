package org.xcasemanager.datacollector

import akka.actor.{ActorSystem, Props}

object Main extends App {

    val actorSystem = ActorSystem.create("collector")
    val httpActor = actorSystem.actorOf(Props[HttpActor],"httpActor")
    val caseExecutionRegisterActor = actorSystem.actorOf(Props[CaseExecutionRegisterActor], "caseExecutionRegisterActor")
    val executionReportMappingActor = actorSystem.actorOf(Props[ExecutionReportMappingActor], "executionReportMappingActor")
    val reportActor = actorSystem.actorOf(Props[ReportActor], "reportActor")
    val logActor = actorSystem.actorOf(Props[LogActor], "logActor")

    httpActor ! StartWebServerCommand
}
