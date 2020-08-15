package org.xcasemanager.datacollector

import java.util.concurrent.TimeUnit
import akka.actor.Actor
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.{Http, HttpExt}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import org.xcasemanager.datacollector.JsonSupport._
import spray.json._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.Option

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import org.mongodb.scala.result.DeleteResult

import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Projections._
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._

/**
  * Actor taking care of configuring and starting the web server
  */
class HttpActor extends Actor {

  implicit val executionContext = context.dispatcher
  implicit val system = context.system
  implicit val timeout = Timeout(10,TimeUnit.SECONDS)
  implicit val materializer = ActorMaterializer()

  var http : HttpExt = null
  var binding : Future[ServerBinding] = null

  // The path to the actor that takes care of registering an execution
  val caseExecutionRegisterActor = context.actorSelection("/user/caseExecutionRegisterActor")
  // The path to the actor that takes care of mapping inbound report result messages
  val executionReportMappingActor = context.actorSelection("/user/executionReportMappingActor")

  override def receive: Receive = {
    // If a StartWebServerCommand is received, then start the web server
    case StartWebServerCommand =>
      if(http == null)
        startWebServer
    // If a StopWebServerCommand is received, then stop the web server
    case StopWebServerCommand =>
      if(binding != null)
        Await.result(binding, 10.seconds)
          .terminate(hardDeadline = 3.seconds)
  }

  /**
    * Configures and starts a web server
    */
  def startWebServer = {
    val routes : Route =
      // endpoint to register a new execution
      path("execution") {
        post {
          entity(as[Registration]) { registration =>
                // Send the message to the registerActor and proceed further without waiting for the response
                caseExecutionRegisterActor ! registration
                // Return a confirmation message
                complete(HttpEntity(ContentTypes.`application/json`, "{\"done\":true}"))
          }
        }
      } ~
      // endpoint to report an execution result
      path("report") {
        post {
          entity(as[Envelope]) { envelope =>
            // On success, forward envelope to the executionReportMappingActor and await its verdict
            onSuccess(executionReportMappingActor ? envelope){
              // If the executionReportMappingActor returns an OpSuccess, then we're good and we print the message
              case res : OpSuccess => complete(StatusCodes.OK, HttpEntity(ContentTypes.`application/json`,res.toJson.prettyPrint))
              // If the executionReportMappingActor returns an OpFailure, then we're not good and we print the message
              case res : OpFailure => complete(StatusCodes.BadRequest,HttpEntity(ContentTypes.`application/json`, res.toJson.prettyPrint))
            }
          }
        }
      } 

      val mongoClient: MongoClient = MongoClient("mongodb://mongodb:27017/")
      val database: MongoDatabase = mongoClient.getDatabase("TCM")
      var collection: MongoCollection[Document] = database.getCollection("Projects")

      val collScores = collection.find().limit(5).projection(fields(include("id", "name", "description"), excludeId()))
      
      context.actorSelection("/user/logActor") ! " -- -------- ------- ----------- "

      val response = Await.result(collScores.toFuture, Duration(10, TimeUnit.SECONDS))

      val listMapScores = response.map(doc=> 
                                                Map("id" -> doc("id").asString.getValue, 
                                                    "name" -> doc("name").asString.getValue,
                                                    "description" -> doc("description").asString.getValue
                                                )
                                      )

      var jsonString = ""
      if(listMapScores.length == 1)
        jsonString = scala.util.parsing.json.JSONObject(listMapScores.head).toString()
      else if(listMapScores.length > 1){
        jsonString += "["
        for (map <- listMapScores) jsonString += ( scala.util.parsing.json.JSONObject(map).toString() + ",")
        jsonString += "]"
      }
        
      if (response != null && response.nonEmpty && response.head.nonEmpty) {
          context.actorSelection("/user/logActor") ! " -- -- -- >> " + response
          context.actorSelection("/user/logActor") ! " -- -- -- >> " + listMapScores
          context.actorSelection("/user/logActor") ! " -- -- -- >> " + jsonString
      }
    
      context.actorSelection("/user/logActor") ! " -- -------- ------- ----------- "

    // Send an asynchronous message to the logActor to say the web server is about to start
    context.actorSelection("/user/logActor") ! "Starting HTTP Server 2"
    // Start and bind the web server
    http = Http()
    binding = http.bindAndHandle(routes, "localhost", 8000)
  }
}