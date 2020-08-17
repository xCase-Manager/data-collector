package org.xcasemanager.datacollector.service

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import org.domain.ProjectRequest
import org.mongodb.scala.Completed
import org.user.repositories.EmployeeRepo

import scala.concurrent.{ExecutionContextExecutor, Future}

class ProjectService {

  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher
  implicit val mat: ActorMaterializer = ActorMaterializer()

  def saveProjectData: ProjectRequest => Future[Completed] = (projectRequest: ProjectRequest) => {
    val projectDoc:Project = projectMapperWithNewID(projectRequest)

    ProjectRepo.insertData(projectDoc)
  }

  def findAll: Source[Project, NotUsed] = {
    Source.fromFuture(ProjectRepo.findAll())
      .mapConcat {
        identity
      }
  }
}
