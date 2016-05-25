package domain

object MessageType extends Enumeration {
  type MessageType = Value
  val userJoinedChat, message, topic, userLeftChat, disconnected,deleteTopic, subscribeTopic = Value
}
