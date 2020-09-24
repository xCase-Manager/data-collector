package org.xcasemanager.datacollector.actors.project

import akka.actor.Actor
import akka.event.Logging
import akka.util.Timeout
import akka.pattern.ask
import akka.pattern.pipe
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Success, Failure}
import org.mongodb.scala.result.DeleteResult
import org.mongodb.scala._
import org.mongodb.scala.Observer
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Projections._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.bson.codecs.Macros._
import org.bson.codecs.configuration.CodecRegistries.{fromRegistries, fromProviders}
import org.mongodb.scala.bson.codecs.Macros.createCodecProvider 
import org.bson.codecs.configuration.CodecRegistries.{fromRegistries, fromProviders}
import org.xcasemanager.datacollector.db.data.Project

/*
    Execution Repository
*/
class ExecutionRepoActor extends Actor {
  val log = Logging(context.system, this)
  import scala.concurrent.ExecutionContext.Implicits.global
  implicit val timeout = Timeout(5 seconds)

  val logActor = context.actorSelection("/user/logActor")
  val codecRegistry = fromRegistries(fromProviders(classOf[Project]), 
                                              MongoClient.DEFAULT_CODEC_REGISTRY)

  val mongoClient = MongoClient("mongodb://mongodb:27017/")
  val database = mongoClient.getDatabase("TCM")
                                              .withCodecRegistry(codecRegistry)

  /*
    message handler
  */
  override def receive: Receive = {
    case id: Long =>
      log.debug(s"id: $id")
      sender ! getProject(id)

    case project: Project =>
      log.debug(s"project: $project")
      sender ! saveProject(project)
  }

  /*
    get project by id
  */
  def getProject(id: Long): Future[Seq[Project]] = {
     var collection: MongoCollection[Project] = database.getCollection("Projects")
     val future = collection.find().projection(
       fields(include("id", "name", "description"), excludeId())).toFuture()
     return future
  }

  /*
    save project
  */
  def saveProject(project: Project): Future[Any] = {
     var collection: MongoCollection[Project] = database.getCollection("Projects")
     return collection.insertOne(project).toFuture()
  }
}