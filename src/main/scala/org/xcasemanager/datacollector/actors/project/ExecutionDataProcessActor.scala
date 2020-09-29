package org.xcasemanager.datacollector.actors.project

import akka.actor.Actor
import scala.util.parsing.json._
import org.xcasemanager.datacollector.db.data.Project

/*
    Data processor
*/
class ExecutionDataProcessActor extends Actor {
  
  /*
    message handler
    @input message
  */
  def receive = {
    case seq: Seq[Project] =>
      sender ! jsonizeDocs(seq)
    
    case project: String =>
      sender ! toProject(project)
  }

  /*
    JSON builder
    @input Projects
    @output String
  */
  def jsonizeDocs(cProject: Seq[Project]): String = {
    val sb=new StringBuilder
    for (proj <- cProject) {
      if (sb.nonEmpty) {
        sb.append(",")
      }
      sb.append("{" + getElement(proj) + "}")
    }
    s"[ $sb.toString ]"
  }

  /*
    Element parser
    @input Project
    @output String
  */
  def getElement(proj: Project): String = {
    val sb=new StringBuilder
    proj.getClass.getDeclaredFields foreach { f =>
      f.setAccessible(true)
      if (sb.nonEmpty) {
        sb.append(",")
      }    
      sb.append(s"'$f.getName': '$f.get($proj)'")   
    }
    sb.toString
  }

  /*
    Project mapper
    @input String
    @input Project
  */
  def toProject(proj: String): Project= {
    val project = JSON.parseFull(proj)
    val map = project.get.asInstanceOf[Map[String, String]]
    return new Project(map.get("id").get.asInstanceOf[String], 
      map.get("name").get.asInstanceOf[String], 
      map.get("description").get.asInstanceOf[String])
  }
}