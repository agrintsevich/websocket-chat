package storage

import com.mongodb.casbah.Imports
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.Logging
import storage.constants.DefaultDocFields._
import domain.{Message, User, Topic}
import java.util.Date
import domain.DomainService

trait StorageComponentImpl extends StorageComponent with Logging {
  override val storage: Storage

  class StorageImpl(implicit context: MongoContext) extends Storage {
    val domainService = new DomainService

    def addUser(user: User) = {
      val builder = MongoDBObject.newBuilder
      builder +=(
        username -> user.name,
        topicName -> user.topics
        )
      context.usersCollection += builder.result()
    }

    def addMessage(message: Message) = {
      val builder = MongoDBObject.newBuilder
      builder +=(
        text -> message.text,
        dateCreated -> message.date,
        topicName -> message.topic,
        user -> message.user
        )
      context.messagesCollection += builder.result()

      print()
    }

    def leaveTopic(user: User, topic: Topic) = {

    }

    def joinTopic(user: User, topic: Topic) = {

    }

    def addTopic(topic: Topic) = context.topicsCollection += topicToDbObject(topic)

    def getTopicByName(topic: String): Topic = {
      val filter = MongoDBObject(topicName -> topic)
      topicToDomain(getTopic(filter))
    }

    def getOldMessages(topic: Topic): Seq[Message] = {
      val filter = MongoDBObject(topicName -> topic.name)

      val messages = getMesages(filter)

      println("FOUND MESSAGES: " + messages)

      for {
        message <- messages
      } yield constractDomainMsq(message)
    }

    private def topicToDbObject(topic: Topic): MongoDBObject = {
      val builder = MongoDBObject.newBuilder
      builder +=(
        topicName -> topic.name,
        createdBy -> topic.createdBy,
        dateCreated -> topic.dateCreated
        )
      builder.result()
    }

    private def getMesages(filter: MongoDBObject) = context.messagesCollection.find(filter).toSeq

    private def getTopic(filter: MongoDBObject): DBObject = context.topicsCollection.findOne().get

    private def topicToDomain(topic: DBObject): Topic = {
      val name = topic.getAs[String](topicName).getOrElse(default)
      val createdByUser = topic.getAs[String](createdBy).getOrElse(default)
      val date = topic.getAs[Date](dateCreated).getOrElse(new Date)
      new Topic(name, domainService.createUser(createdByUser), date)
    }

    private def constractDomainMsq(message: Imports.DBObject): Message = {
      val messageText = message.getAs[String](text).getOrElse(default)
      val date = message.getAs[Date](dateCreated).getOrElse(new Date)
      val topic = message.getAs[String](topicName).getOrElse(default)
      val userName = message.getAs[String](user).getOrElse(default)
      new Message(messageText, date, domainService.createUser(username), getTopicByName(topic))
    }
  }

}
