package domain

import java.util.Date

import play.api.libs.json.JsValue
import constants._

class DomainService {

  def createUser(name: String, topics: Option[Seq[Topic]] = None): User = new User(name, topics.get)

  def createUser(name: String) = new User(name, null)

  def createMessage(text: String, date: Date, user: User, topic: Topic) = new Message(text,date,user, topic)

  def createMessage(data: JsValue): Message = {
    val username =  (data \ "username").get.toString.replace("\"","")
    val message = (data \ "data").get.toString.replace("\"","")
    val topicName = (data \ "topic").get.toString.replace("\"","")
    val msgType = (data \ "type")

    val user = createUser(username)
    val topic = createTopic(topicName, user, new Date())

    new Message(message, new Date(), user, topic)
  }

  def createTopic(name: String, user: User,date: Date) = new Topic(name, user, date)

}
