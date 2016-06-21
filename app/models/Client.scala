package models

import akka.actor._
import models.Messages._
import play.api.libs.json.JsValue

class Client(out: ActorRef, chat: ActorRef, topic: String) extends Actor {

  chat ! Join(topic)

  override def postStop() = chat ! Leave

  def receive = {
    case text: JsValue =>
      chat ! ClientSentMessage(text)

    case ClientSentMessage(text) => {
      out ! text
    }

    case ClientCreatedTopic(client, topic) => {
      out ! topic
    }

    case ClientJoinedChat(client) => {
      println("Client joined chat: "+client)
      chat ! ClientJoinedChat(client)
    }

    case ClientLeftChat(client) => {
      chat ! ClientLeftChat(client)
    }

  }
}
