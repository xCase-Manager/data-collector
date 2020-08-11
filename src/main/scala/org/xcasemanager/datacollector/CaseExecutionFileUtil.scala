package org.xcasemanager.datacollector

import java.io.{File, FileWriter}
import org.xcasemanager.datacollector.JsonSupport._
import spray.json._
import scala.io.Source

/**
  * Utilities to read and write files
  */
object CaseExecutionFileUtil {

  /**
    * File where case executions is saved
    */
  val executionsFile = new File("caseExecutions.json")

  /**
    * Directory where case execution results are saved
    */
  val resultsDirectory = new File("caseExecutionResults")

  /**
    * Loads and deserializes exections from a file
    * @param file a file
    * @return the deserialized executions
    */
  def loadExecutionsFromFile(): Map[String, Registration] = {
    if (executionsFile.exists()) {
      val source = Source.fromFile(executionsFile)
      val data = source.mkString
      if(data.isEmpty)
        return Map[String,Registration]()
      source.close()
      val dataFromFile: Map[String, Registration] = data.parseJson.convertTo[Map[String, Registration]]
      return dataFromFile
    }
    return Map[String, Registration]()
  }

  /**
    * Serializes and saves a case execution to a file
    * @param file a file
    * @param items a map of case execution
    */
  def saveCaseExectionToFile(items: Map[String, Registration]): Unit = {
    if (!executionsFile.exists())
      executionsFile.createNewFile()
    val fileWriter = new FileWriter(executionsFile)
    fileWriter.write(items.toJson.prettyPrint)
    fileWriter.close()
  }

  /**
    * Serializes and appends an execution result to a file
    * @param file a file
    * @param message a execution result
    */
  def appendResultToFile(result: Envelope): Unit = {
    val resultFile = new File(resultsDirectory.getAbsolutePath + File.separator + result.recipient + ".txt")
    if (!resultFile.exists())
      resultFile.createNewFile()
    val fileWriter = new FileWriter(resultFile,true)
    fileWriter.append(result.toJson.compactPrint+"\n")
    fileWriter.close()
  }

}
