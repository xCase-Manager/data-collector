package org.xcasemanager.datacollector.repositories

import org.db.config.DbConfig
import org.db.data.Employee
import org.mongodb.scala.{Completed, MongoCollection}
import org.utils.JsonUtils

import scala.concurrent.Future

object EmployeeRepo extends JsonUtils {
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