package storage

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.Logging
import storage.constants.DefaultDocFields._
import domain.{Message, User, Topic}

trait StorageComponentImpl extends StorageComponent with Logging{
  override val storage: Storage

  class StorageImpl(implicit context: MongoContext) extends Storage {
    def addUser(user: User) = {
      val builder = MongoDBObject.newBuilder
      builder += (
        username -> user.name,
        topicName -> user.topics
        )
      context.usersCollection += builder.result()
    }

    def addMessage(message: Message) = {
      val builder = MongoDBObject.newBuilder
      builder += (
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

    def addTopic(topic: Topic) = {
      val builder = MongoDBObject.newBuilder
      builder += (
        topicName -> topic.name,
        createdBy -> topic.createdBy,
        dateCreated -> topic.dateCreated
        )
      context.topicsCollection += builder.result()
    }

    def getOldMessages(topic: Topic)= {

    }

    def print() = {
      val itr = context.topicsCollection.find()
      while (itr.hasNext)
        println(itr.next())

      val itr1 = context.messagesCollection.find()
      while (itr1.hasNext)
        println(itr1.next())
    }

  }

}
