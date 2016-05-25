package controllers

import javax.inject._
import akka.actor._
import play.api._
import play.api.libs.json.JsValue
import play.api.mvc._
import play.api.Play.current

import models._
import models.Messages._
import storage.{StorageService, StorageComponentImpl}
import domain.{User, DomainService}


@Singleton
class Application @Inject()(actorSystem: ActorSystem) extends Controller {

  val chat = actorSystem.actorOf(Props[Chat], "chat")

  val domainService = new DomainService
  val storage = StorageService.getStorage

  def socket(topic: String) = WebSocket.acceptWithActor[JsValue, JsValue] {
    (request: RequestHeader) =>
      (out: ActorRef) =>
        Props(new Client(out, chat, topic))
  }

  def joinChat(username: String, topic: String) = Action { implicit request =>
    if (username == null || (username.trim == "")) {
      Redirect(routes.Application.index).flashing(
        "error" -> "Please choose a valid username."
      )
    } else {
      storage.addUser(domainService.createUser(username))
      val user = domainService.createUser(username)
      val topics = storage.getTopicsOfUser(user).toList

      Ok(views.html.index(username, topic, topics))
    }
  }

  def createTopic(topic: String) = Action { implicit request =>
    if (topic == null || (topic.trim == "")) {
      Redirect(routes.Application.index).flashing(
        "error" -> "Please choose a valid topic name."
      )
    } else {
      Props(new Topic(topic, chat))
      Ok
    }
  }

  def index = Action {
    Ok(views.html.index(null, "", Seq.empty[domain.Topic]))
  }
}
