package storage

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.Logging
import constants.DefaultDocFields._
import domain.{Message, User, Topic}

trait StorageComponentImpl extends StorageComponent with Logging{
  override val storage: Storage

  class StorageImpl(implicit context: MongoContext) extends Storage {
    def addUser(user: User) = {
      val builder = MongoDBObject.newBuilder
      builder += (
        username -> user.name,
        topic -> user.topics
        )
      context.usersCollection += builder.result()

      println("USERS: "+context.usersCollection.find().next())
    }

    def addMessage(message: Message) = {
      val builder = MongoDBObject.newBuilder
      builder += (
        text -> message.text,
        dateCreated -> message.date,
        topic -> message.topic,
        user -> message.user
        )
      context.messagesCollection += builder.result()

      val itr = context.messagesCollection.find()

      println("-------------------------------------")
      while (itr.hasNext)
        println("Msg: "+itr.next())


    }


    def leaveTopic(user: User, topic: Topic) = ???

    def joinTopic(user: User, topic: Topic) = ???

    def addTopic(topic: Topic) = ???
  }

}
