package controllers

import javax.inject._
import akka.actor._
import play.api._
import play.api.libs.json.JsValue
import play.api.mvc._
import play.api.Play.current

import models._
import models.Messages._


@Singleton
class Application @Inject()(actorSystem: ActorSystem) extends Controller {

  val chat = actorSystem.actorOf(Props[Chat], "chat")

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
      Ok(views.html.index(username, topic))
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
    Ok(views.html.index(null, ""))
  }
}
