package org.xcasemanager.datacollector.queue.message

import akka.kafka.ConsumerMessage
import org.xcasemanager.datacollector.message.Project

object ExecutionMessages {

  case class InputMessage(project: Project, offset: ConsumerMessage.CommittableOffset)

}