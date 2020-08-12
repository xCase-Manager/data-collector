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

    // Send an asynchronous message to the logActor to say the web server is about to start
    context.actorSelection("/user/logActor") ! "Starting HTTP Server"
    // Start and bind the web server
    http = Http()
    binding = http.bindAndHandle(routes, "localhost", 8000)
  }
}
