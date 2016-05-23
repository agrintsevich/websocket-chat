package models

import akka.actor.ActorRef
import play.api.libs.json.JsValue

object Messages {
  case class Join(topic: String)
  case object Leave

  final case class ClientSentMessage(text: JsValue)

  final case class ClientCreatedTopic(client: ActorRef, topic: String)

  final case class ClientJoinedChat(client: ActorRef)

  final case class ClientLeftChat(client: ActorRef)
}
