package domain

import java.util.Date

import domain.MessageType.MessageType
import play.api.libs.json.JsValue

class DomainService {

  def createUser(name: String, topics: Option[Seq[Topic]] = None): User = new User(name, topics.get)

  def createUser(name: String) = new User(name, null)

  def createUser(data: JsValue) = {
    val username =  (data \ "username").get.toString.replace("\"","")
    new User(username, null)
  }

  def getMsgType(data: JsValue): MessageType.Value = {
    MessageType.withName((data \ "type").get.toString.replace("\"",""))
  }

  def createMessage(text: String, date: Date, user: User, topic: Topic) = new Message(text,date,user, topic)

  def createMessage(data: JsValue): Message = {
   parseMessage(data)
  }

  def createTopic(name: String, user: User,date: Date): Topic = new Topic(name, user, date)

  def createTopic(data: JsValue): Topic = {
    val username =  (data \ "username").get.toString.replace("\"","")
    val topicName = (data \ "topic").get.toString.replace("\"","")
    val user = createUser(username)
    createTopic(topicName, user, new Date())
  }

  private def parseMessage(data: JsValue): Message = {
    val username =  (data \ "username").get.toString.replace("\"","")
    val message = (data \ "data").get.toString.replace("\"","")
    val topicName = (data \ "topic").get.toString.replace("\"","")

    val user = createUser(username)
    val topic = createTopic(topicName, user, new Date())

    new Message(message, new Date(), user, topic)
  }

}
