package org.xcasemanager.datacollector

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

/**
  * Project message
  * @param id project id
  * @param name project name
  * @param description project description
  */
case class Project(val id : String, val name : String, val description : String)

/**
  * Success message
  * @param message the message
  */
case class OpSuccess(val message : String)

/**
  * Failure message
  * @param message the message
  */
case class OpFailure(val message : String)

/**
  * Json formatters
  */
object JsonSupport extends SprayJsonSupport with DefaultJsonProtocol { 
  implicit val oProjectFormat = jsonFormat3(Project)
  implicit val opSuccessFormat = jsonFormat1(OpSuccess)
  implicit val opFailureFormat = jsonFormat1(OpFailure)
}