package org.xcasemanager.datacollector.actors.project

import akka.actor.Actor
import akka.pattern.ask
import akka.util.Timeout
import akka.pattern.pipe

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Success, Failure}

// MongoDB
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

//xcasemanager
import org.xcasemanager.datacollector.db.data.Project


class ExecutionRepoActor extends Actor {
  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val timeout = Timeout(5 seconds)

  val logActor = context.actorSelection("/user/logActor")
  val codecRegistry = fromRegistries(fromProviders(classOf[Project]), MongoClient.DEFAULT_CODEC_REGISTRY)


  override def receive: Receive = {

    case l: Long =>
      println(s"receive a long variable: $l")
      sender ! getProject(12)
  }


  /*
    get project by id
  */
  def getProject(id: Int): Future[Seq[Project]] = {
     logActor ! " ---------->> requesting DB for project id " + id
     val mongoClient: MongoClient = MongoClient("mongodb://mongodb:27017/")
     val database: MongoDatabase = mongoClient.getDatabase("TCM")
                                              .withCodecRegistry(codecRegistry)
     var collection: MongoCollection[Project] = database.getCollection("Projects")

     // return Future[Seq[Document]]
     val future = collection.find().limit(5).projection(fields(include("id", "name", "description"), excludeId())).toFuture()
     logActor ! " ---------->> we got the DB future " + future
     return future
  }
}