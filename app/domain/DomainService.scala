package domain

import java.util.Date

import play.api.libs.json.JsValue

class DomainService {

  def createUser(name: String, topics: Option[Seq[Topic]] = None): User = new User(name, topics.get)

  def createUser(name: String) = new User(name)

  def createUser(data: JsValue) = {
    val username = (data \ "username").get.toString.replace("\"", "")
    new User(username, Seq.empty[Topic])
  }

  def getMsgType(data: JsValue): MessageType.Value = {
    (data \ "type").get match {
      case value: JsValue => MessageType.withName(value.toString.replace("\"", ""))
      case null => MessageType.message
    }
  }

  def createMessage(text: String, date: Date, user: User, topic: Topic) = new Message(text, date, user, Some(topic))

  def createMessage(data: JsValue): Message = {
    parseMessage(data)
  }

  def createTopic(name: String, user: User, date: Date): Topic = new Topic(name, user, date)

  def createTopic(name: String): Topic = new Topic(name, null, new Date)

  def createTopic(data: JsValue): Topic = {

    val username = (data \ "username").get.toString.replace("\"", "")
    val user = createUser(username)

    val topicName = if ((data \ "data").toOption.isDefined) {
      (data \ "data").get.toString.replace("\"", "")
    } else if ((data \ "topic").toOption.isDefined){
      (data \ "topic").get.toString.replace("\"", "")
    } else ""
    createTopic(topicName, user, new Date())
  }

  private def parseMessage(data: JsValue): Message = {
    val username = (data \ "username").get.toString.replace("\"", "")
    val message = (data \ "data").get.toString.replace("\"", "")
    val topicName = (data \ "topic").get.toString.replace("\"", "")

    val user = createUser(username)
    val topic = createTopic(topicName, user, new Date())

    new Message(message, new Date(), user, Some(topic))
  }

}
