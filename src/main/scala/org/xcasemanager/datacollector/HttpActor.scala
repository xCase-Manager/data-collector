package org.xcasemanager.datacollector

import akka.actor.Actor
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.{Http, HttpExt}
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.pattern.ask
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import scala.util.{Failure, Success}
import akka.event.Logging
import org.xcasemanager.datacollector.JsonSupport._

/**
* data collector API server
*/
class HttpActor extends Actor {

  val log = Logging(context.system, this)
  val executionDataProcessActor = 
    context.actorSelection("/user/executionDataProcessActor")
  val executionRepoActor = context.actorSelection("/user/executionRepoActor")
  val errorMessage = "{\"error\": \"could not save project\"}"
  /*
    Exception Handler
  */
  implicit def serverExceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case error: Exception =>
        extractUri { uri =>
          log.info(s"request to $uri could not be handled normally. " +
            s"Reason: $error.getMessage")
          complete(HttpEntity(ContentTypes.`application/json`, 
            errorMessage))
        }
    }
  implicit val executionContext = context.dispatcher
  implicit val system = context.system
  implicit val timeout = Timeout(10,TimeUnit.SECONDS)

  var http : HttpExt = null
  var binding : Future[ServerBinding] = null

  /*
    server command messages handler
    @input message
  */
  override def receive: Receive = {
    case StartWebServerCommand =>
      if(http == null)
        startWebServer
    case StopWebServerCommand =>
      if(binding != null)
        Await.result(binding, 10.seconds)
          .terminate(hardDeadline = 3.seconds)
  }

  /**
  * Server resources definition
  */
  def startWebServer = {
    val routes : Route = Route.seal(
      concat(
        get {
          pathPrefix("project" / LongNumber) { id =>
            val proj: Future[Any] = executionRepoActor ? id
            onComplete(proj) {
              case Success(seqFuture: Future[Any]) => {
                onComplete(seqFuture) {
                  case Success(seq) => {
                    val projStr: Future[Any] = executionDataProcessActor ? seq
                    onComplete(projStr) {
                      case Success(seqStr: String) => {
                        complete(HttpEntity(ContentTypes.`application/json`, seqStr))
                      }
                      case Failure(failure) => 
                        complete(HttpEntity(ContentTypes.`application/json`, 
                        errorMessage)) 
                      }
                    }            
                    case Failure(failure) => 
                      complete(HttpEntity(ContentTypes.`application/json`, 
                      errorMessage))  
                }        
              }                             
              case Failure(failure) => {
                complete(HttpEntity(ContentTypes.`application/json`,
                errorMessage))    
              }              
            }       
          }
        } ~
        post {
          pathPrefix("project") {
            entity(as[String]) { projectStr =>
              val projFut: Future[Any] = executionDataProcessActor ? projectStr
              println("projFut: " + projFut)
              onComplete(projFut) {
                case Success(project: org.xcasemanager.datacollector.db.data.Project) => {
                  val proj: Future[Any] = executionRepoActor ? project
                  onComplete(proj) {
                    case Success(seqFuture: Future[Any]) => {
                      onComplete(seqFuture) {
                        case Success(res: Any) => {        
                          complete(Created, HttpEntity.Empty)                  
                        }                         
                        case Failure(failure) =>
                          complete(HttpEntity(ContentTypes.`application/json`, 
                          errorMessage))             
                        }
                    }
                    case Failure(failure) =>
                      complete(HttpEntity(ContentTypes.`application/json`, 
                      errorMessage))
                  }     
                }
                case Failure(failure) => 
                  complete(HttpEntity(ContentTypes.`application/json`, 
                  errorMessage))        
              }
            }
          }      
        }
      )
    )

    // Start server
    log.info("Starting Data Collector API Server ...")
    http = Http()
    binding = http.bindAndHandle(routes, "localhost", 8000)
  }
}
