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
        val dbuser = userToDbObj(user)
        context.usersCollection += dbuser
      }
    }

    def addMessage(message: Message) = {
      val builder = MongoDBObject.newBuilder
      builder +=(
        text -> message.text,
        dateCreated -> message.date,
        topicName -> message.topic,
        user -> userToDbObj(message.user)
        )
      context.messagesCollection += builder.result()
    }

    def leaveTopic(user: User, topic: Topic) = {
      val userDbObj = getUserDbObject(user.name)
      userDbObj match {
        case Some(foundUser) => {
          val userTopics = userToDomain(foundUser).topics
          val newTopics = userTopics.filterNot(_.name.equals(topic.name))
          val transformedToDbTopics = for {
            t <- newTopics
          }yield (topicToDbObject(t))

          context.topicsCollection.findAndRemove(MongoDBObject(topicName -> topic.name))
          context.usersCollection.update(MongoDBObject(username -> user.name), $set (topics->transformedToDbTopics))
        }
        case None => {}
      }
    }

    def joinTopic(user: User, topic: Topic) = {
      val dbUser = getUserDbObject(user.name)
      dbUser match {
        case Some(foundUser) => {
          val existingTopics = userToDomain(foundUser).topics
          existingTopics.find(_.name.equals(topic.name)) match {
            case Some(_) => {
            }
            case None => {
              val newTopics = (existingTopics ++ List(topic))
              val transformedToDbTopics = for {
                t <- newTopics
              }yield (topicToDbObject(t))

              context.usersCollection.update(MongoDBObject(username->user.name), $set (topics->transformedToDbTopics))
            }
           }

        }
        case None => {}
      }
    }

    def getTopicsOfUser(user: User): Seq[Topic] = {
      val topicsCreatedBy = getTopics(MongoDBObject(createdBy -> user.name))
      val dbUser = getUserByName(user.name)
      val topicsSeq =
        for {
          topic_ <- topicsCreatedBy
        } yield topicToDomain(topic_)


      if (dbUser.isDefined) {
        return topicsSeq.toSeq ++ dbUser.get.topics
      }
      return topicsSeq.toSeq
    }

    def addTopic(topic: Topic) = {
      if (!getTopic(MongoDBObject(topicName -> topic.name)).isDefined) {
        context.topicsCollection += topicToDbObject(topic)
      }
    }

    def getTopicByName(topic: String): Option[Topic] = {
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
      } yield constructDomainMsq(message)
    }

    def getUserByName(name: String): Option[User] = {

      val dbUser = context.usersCollection.findOne(MongoDBObject(username -> name))
      dbUser match {
        case Some(user) => return Some(userToDomain(user))
        case None => {
          None
        }
      }

    }

    private def updateUser(old: User, newUser: User) = {

    }

    private def getUserDbObject(name: String): Option[DBObject] = {
      val userItr = context.usersCollection.find(MongoDBObject(username -> name))
      if (userItr.hasNext) {
        val user = userItr.next
        return Some(user)
      }
      None
    }

    private def userToDomain(user: DBObject): User = {
      if (!user.isEmpty) {
        val name = user.getAs[String](username).get
        val topicsSeq = user.getAs[MongoDBList](topics).getOrElse(Seq.empty[Topic]).asInstanceOf[Seq[DBObject]]
        val userTopics =
          for {
            topic_ <- topicsSeq
          } yield topicToDomain((topic_))

       return new User(name, userTopics)
      } else {
        new User("Bot", Seq.empty[Topic])
      }
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

    private def userToDbObj(user: User): DBObject = {

      val userTopics = for {
        t <- user.topics
      } yield (topicToDbObject(t))

      val builder = MongoDBObject.newBuilder
      builder +=(
        username -> user.name,
        topics -> userTopics
        )
      builder.result()
    }

    private def topicToDomain(topic: DBObject): Topic = {
      val name = topic.getAs[String](topicName).getOrElse(default)
      val createdByUser = topic.getAs[String](createdBy).getOrElse(default)
      val date = topic.getAs[Date](dateCreated).getOrElse(new Date)
      new Topic(name, domainService.createUser(createdByUser), date)
    }

    private def constructDomainMsq(message: Imports.DBObject): Message = {
      println("LOAD MESSAGE: "+message)

      val messageText = message.getAs[String](text).getOrElse(default)
      val date = message.getAs[Date](dateCreated).getOrElse(new Date)
      val topic = message.getAs[String](topicName).getOrElse(default)
      val user_ = userToDomain(message.getAs[DBObject](user).getOrElse(DBObject.empty))
      new Message(messageText, date, user_, getTopicByName(topic))
    }
  }

}
