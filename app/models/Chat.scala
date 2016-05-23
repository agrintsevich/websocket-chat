package models

import akka.actor._

import Messages._
import storage.{MongoConfig, MongoContext, StorageComponentImpl}
import domain.DomainService
import java.util.Date

class Chat extends Actor {
  implicit val mongoContext = new MongoContext(new MongoConfig())

  val storage = new StorageComponentImpl  {
    override val storage: Storage = new StorageImpl
  }.storage

  val domainService = new DomainService


  def receive = process(Set.empty)

  def process(subscribers: Set[ActorRef]): Receive = {
    case Join(topic: String) => {
      storage.addUser(domainService.createUser("Test"))
      subscribers.foreach {_ ! ClientJoinedChat(sender)}
      context become process(subscribers + sender)
    }

    case Leave => {
      subscribers.foreach {_ ! ClientLeftChat(sender)}
      context become process(subscribers - sender)
    }

    case msg: ClientSentMessage => {
      storage.addMessage(domainService.createMessage(msg.text))

      (subscribers - sender).foreach {
        _ ! msg
      }
    }

    case topic: ClientCreatedTopic =>
      (subscribers).foreach {_ ! topic}


  }
}
