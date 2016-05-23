package models

import akka.actor.{Actor, ActorRef}
import models.Messages.{Leave, ClientCreatedTopic}


class Topic (topic: String, chat: ActorRef) extends Actor {

  chat ! ClientCreatedTopic(sender, topic)

  override def postStop() = chat ! Leave

  def receive = {
    case _ => ???
  }
}

