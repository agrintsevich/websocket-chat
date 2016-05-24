package models

import akka.actor._

import Messages._
import domain.MessageType._
import storage.{MongoConfig, MongoContext, StorageComponentImpl}
import domain.{DomainService}
import domain.MessageType

class Chat extends Actor {
  implicit val mongoContext = new MongoContext(new MongoConfig())

  val storage = new StorageComponentImpl {
    override val storage: Storage = new StorageImpl
  }.storage

  val domainService = new DomainService

  def receive = process(Set.empty)

  def process(subscribers: Set[ActorRef]): Receive = {
    case Join(topic: String) => {
      subscribers.foreach {
        _ ! ClientJoinedChat(sender)
      }
      context become process(subscribers + sender)
    }

    case Leave => {
      subscribers.foreach {
        _ ! ClientLeftChat(sender)
      }
      context become process(subscribers - sender)
    }

    case msg: ClientSentMessage => {

      domainService.getMsgType(msg.text) match {
        case MessageType.userJoinedChat =>  {
          val topic = domainService.createTopic(msg.text)
          val user = domainService.createUser(msg.text)
          storage.addUser(user)
          storage.joinTopic(user, topic)
        }
        case MessageType.message => storage.addMessage(domainService.createMessage(msg.text))
        case MessageType.topic => {
          val user = domainService.createUser(msg.text)
          val topic = domainService.createTopic(msg.text)
          storage.addTopic(domainService.createTopic(msg.text))
          storage.joinTopic(user, topic)
        }
        case MessageType.userLeftChat => storage.leaveTopic(domainService.createUser(msg.text), domainService.createTopic(msg.text))
        case MessageType.disconnected => {}
      }

      (subscribers - sender).foreach {
        _ ! msg
      }
    }

    case topic: ClientCreatedTopic =>
      (subscribers).foreach {
        _ ! topic
      }


  }
}
