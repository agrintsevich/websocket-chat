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
      if (!getUserByName(user.name).isDefined) {
        val userTopics = for {
          t <- user.topics
        } yield (topicToDbObject(t))

        val builder = MongoDBObject.newBuilder
        builder +=(
          username -> user.name,
          topics -> user.topics
          )
        context.usersCollection += builder.result()
      }
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
    }

    def leaveTopic(user: User, topic: Topic) = {

      val userDbObj = getUserDbObject(user.name)
      userDbObj match {
        case Some(foundUser) => {
          val newTopics = user.topics
          foundUser.update(topics, newTopics.filterNot(_ equals (topic)))
        }
        case None => {}
      }
    }

    def joinTopic(user: User, topic: Topic) = {
      val dbUser = getUserDbObject(user.name)

      dbUser match {
        case Some(userVal) => {
          val existingTopics = user.topics
          userVal.update(topics, existingTopics :+ topicToDbObject(topic))
        }
        case None => {}
      }
    }

    def getTopicsOfUser(user: User): Seq[Topic] = {
      val topicsCreatedBy = getTopics(MongoDBObject(createdBy -> user.name))
      val topicsSeq =
        for {
          topic_ <- topicsCreatedBy
        } yield topicToDomain(topic_)

      topicsSeq.toSeq ++ user.topics
    }

    def addTopic(topic: Topic) = {
      if (!getTopic(MongoDBObject(topicName -> topic.name)).isDefined)
        context.topicsCollection += topicToDbObject(topic)
    }

    def getTopicByName(topic: String):Option[Topic] = {
      val filter = MongoDBObject(topicName -> topic)
      if (getTopic(filter).isDefined)
        return Some(topicToDomain(getTopic(filter).get))
      None
    }

    def getOldMessages(topic: Topic): Seq[Message] = {
      val filter = MongoDBObject(topicName -> topic.name)
      val messages = getMesages(filter)

      for {
        message <- messages
      } yield constractDomainMsq(message)
    }

    def getUserByName(name: String): Option[User] = {
      val userItr = context.usersCollection.find(MongoDBObject(username -> name))
      if (userItr.hasNext) {
        val user = userItr.next
        return Some(userToDomain(user))
      }
      None
    }

    private def updateUser(old: User, newUser: User) = {

    }

    private def getUserDbObject(name: String): Option[DBObject] = {
      val userItr = context.usersCollection.find(MongoDBObject(username -> name))
      if (userItr.hasNext) {
        println ("COUNT OF USERS WITH NAME "+name+" - "+userItr.size)
        val user = userItr.next
        return Some(user)
      }
      None
    }

    private def userToDomain(user: MongoDBObject): User = {
      val name = user.getAs[String](username).get
      val topicsSeq = user.getAs[MongoDBList](topics).getOrElse(Seq.empty[Topic])
      val userTopics =
        for {
          topic_ <- topicsSeq
        } yield domainService.createTopic(String.valueOf(topic_))

      new User(name, userTopics)
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

    private def getTopic(filter: MongoDBObject): Option[DBObject] = getTopics(filter) match {
      case Seq(el, _) => Some(el)
      case _ => None
    }

    private def getTopics(filter: MongoDBObject): Seq[DBObject] = context.topicsCollection.find(filter).toSeq

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
