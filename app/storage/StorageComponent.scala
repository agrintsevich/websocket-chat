package storage

import domain._

trait StorageComponent {
  val storage: Storage

  trait Storage {
    def addUser(user: User)

    def addTopic(topic: Topic)

    def addMessage(message: Message)

    def joinTopic(user: User, topic: Topic)

    def getTopicByName(topic: String): Option[Topic]

    def leaveTopic(user: User, topic: Topic)

    def getTopicsOfUser(user: User): Seq[Topic]

    def getOldMessages(topic: Topic): Seq[Message]

    def getUserByName(name: String): Option[User]
  }
}
