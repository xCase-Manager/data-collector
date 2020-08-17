package org.xcasemanager.datacollector.repositories

import org.xcasemanager.datacollector.db.config.DbConfig
import org.xcasemanager.datacollector.db.data.Project
import org.mongodb.scala.{Completed, MongoCollection}
import org.utils.JsonUtils

import scala.concurrent.Future

object ProjectRepo extends JsonUtils {
  private val projectDoc: MongoCollection[Project] = DbConfig.projects

  def createCollection(): Unit = {
    DbConfig.database.createCollection("project").subscribe(
      (result: Completed) => println(s"$result"),
      (e: Throwable) => println(e.getLocalizedMessage),
      () => println("complete"))
  }

  def insertData(proj: Project): Future[Completed] = {
    projectDoc.insertOne(proj).toFuture()
  }

  def findAll(): Future[Seq[Project]] = {
    projectDoc.find().toFuture()
  }
}