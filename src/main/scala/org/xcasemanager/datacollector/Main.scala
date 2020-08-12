package org.xcasemanager.datacollector

import akka.actor.{ActorSystem, Props}

/*
    http://mongodb.github.io/mongo-scala-driver/2.2/bson/macros/
*/
import com.mongodb.async.client.MongoClients
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.bson.codecs.configuration.CodecRegistries.{fromRegistries, fromProviders}
import org.mongodb.scala.bson.codecs.Macros._

object Main extends App {

    val actorSystem = ActorSystem.create("collector")
    val httpActor = actorSystem.actorOf(Props[HttpActor],"httpActor")
    val caseExecutionRegisterActor = actorSystem.actorOf(Props[CaseExecutionRegisterActor], "caseExecutionRegisterActor")
    val executionReportMappingActor = actorSystem.actorOf(Props[ExecutionReportMappingActor], "executionReportMappingActor")
    val reportActor = actorSystem.actorOf(Props[ReportActor], "reportActor")
    val logActor = actorSystem.actorOf(Props[LogActor], "logActor")

    case class Number(_id: Int)
    val codecRegistry = fromRegistries(fromProviders(classOf[Number]), DEFAULT_CODEC_REGISTRY)
    private val client = MongoClients.create("mongodb://localhost:27017")
    private val db = client.getDatabase("TCM")
    private val numbersColl = db
    .getCollection("Projects", classOf[Number])
    .withCodecRegistry(codecRegistry)

    httpActor ! StartWebServerCommand
}
