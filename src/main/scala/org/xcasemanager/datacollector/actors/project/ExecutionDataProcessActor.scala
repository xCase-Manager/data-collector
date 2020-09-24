package org.xcasemanager.datacollector.actors.project

import akka.actor.Actor
import akka.event.Logging
import scala.util.parsing.json._
import org.xcasemanager.datacollector.db.data.Project

/*
    Data processor
*/
class ExecutionDataProcessActor extends Actor {
  
  val log = Logging(context.system, this)

  /*
    message handler
  */
  def receive = {
    case seq: Seq[Project] =>
      log.debug(s"project: $seq")
      sender ! jsonizeDocs(seq)
    
    case project: String =>
      log.debug(s"project: $project")
      sender ! toProject(project)
  }

  /*
    main JSON builder
  */
  def jsonizeDocs(cProject: Seq[Project]): String = {
    val sb=new StringBuilder
    for (proj <- cProject) {
      if (sb.nonEmpty) {
        sb.append(",")
      }
      sb.append("{" + getElement(proj) + "}")
    }
    "[" + sb.toString + "]"
  }

  /*
    JSON element builder
  */
  def getElement(proj: Project): String = {
    val sb=new StringBuilder
    proj.getClass.getDeclaredFields foreach { f =>
      f.setAccessible(true)
      if (sb.nonEmpty) {
        sb.append(",")
      }    
      sb.append("'" + f.getName + "': '" + f.get(proj) + "'")   
    }
    sb.toString
  }

  /*
    convert to Project
  */
  def toProject(proj: String): Project= {
    val project = JSON.parseFull(proj)
    val map = project.get.asInstanceOf[Map[String, String]]
    return new Project(map.get("id").get.asInstanceOf[String], 
      map.get("name").get.asInstanceOf[String], 
      map.get("description").get.asInstanceOf[String])
  }
}