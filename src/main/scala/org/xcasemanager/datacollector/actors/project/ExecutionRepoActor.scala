package org.xcasemanager.datacollector.actors.project

import akka.actor.{Actor, ActorLogging}
import scala.concurrent.Future
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.model.Projections._
import org.bson.codecs.configuration.CodecRegistries
  .{fromRegistries, fromProviders}
import org.xcasemanager.datacollector.db.data.Project


/*
    Execution Repository
*/
class ExecutionRepoActor extends Actor with ActorLogging{
  import scala.concurrent.ExecutionContext.Implicits.global
  val codecRegistry = 
    fromRegistries(fromProviders(classOf[Project]), 
    MongoClient.DEFAULT_CODEC_REGISTRY)
  implicit val executionContext = context.dispatcher
  implicit val system = context.system
  val config = system.settings.config
  val mongoClient = MongoClient(config.getString(
    "DataCollector.database.connect"))
  val database = mongoClient.getDatabase(
    config.getString("DataCollector.database.connect"))
    .withCodecRegistry(codecRegistry)

  /*
    message handler
    @input message
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
    get project
    @input project id
    @output Future
  */
  def getProject(id: Long): Future[Seq[Project]] = {
     var collection: MongoCollection[Project] = 
      database.getCollection("Projects")
     val future = collection.find().projection(
       fields(include("id", "name", "description"), 
        excludeId())).toFuture()
     return future
  }

  /*
    save project
    @input Project
    @output Future
  */
  def saveProject(project: Project): Future[Any] = {
     var collection: MongoCollection[Project] = 
      database.getCollection("Projects")
     return collection.insertOne(project).toFuture()
  }
}