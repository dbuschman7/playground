package controllers

import play.api.mvc._
import play.api.libs.concurrent.Akka
import actors.UserChannelsActor
import akka.actor.{ Props }
import scala.concurrent.duration._
import play.api.Play.current
import models.{ StartSearch, SearchFeed }
import play.api.libs.EventSource
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import models.StartSearch

object Application extends Controller {

  implicit val timeout = Timeout(5 seconds)

  val channels = Akka.system.actorSelection("/user/channels")

  def home = Action {
    Ok(views.html.home())
  }

  def search(searchString: String) = Action.async {
    (channels ? StartSearch(searchString = searchString)).map {
      case SearchFeed(out) => Ok.chunked(out &> EventSource()).as("text/event-stream")
    }
  }

  def real = Action {
    Ok(views.html.real())
  }

  def mongofs = Action {
    Ok(views.html.mongofs())
  }

  def cookbook = Action {
    Ok(views.html.cookbook("Cookbook"))
  }
}