package storage

import domain._

trait StorageComponent {
  val storage: Storage

  trait Storage {
    def addUser(user: User)

    def addTopic(topic: Topic)

    def addMessage(message: Message)

    def joinTopic(user: User, topic: Topic)

    def leaveTopic(user: User, topic: Topic)
  }
}
