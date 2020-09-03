package org.xcasemanager.datacollector.actors.project

import akka.actor.Actor
import org.xcasemanager.datacollector.db.data.Project

class ExecutionDataProcessActor extends Actor {
 
  /*
    message handler
  */
  def receive = {

    case seq: Seq[Project] =>
      println(s"increment $seq")
      sender ! jsonizeDocs(seq)
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
}