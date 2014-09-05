package controllers

import play.api.mvc._
import play.api.libs.concurrent.Akka
import actors.MainSearchActor
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

  val mainSearch = Akka.system.actorSelection("/user/channelSearch")

  def index = Action {
    Ok(views.html.index("Reactive Demo App"))
  }

  def search(searchString: String) = Action.async {
    (mainSearch ? StartSearch(searchString = searchString)).map {
      case SearchFeed(out) => Ok.chunked(out &> EventSource()).as("text/event-stream")
    }
  }

  def main = Action {
    Ok(views.html.main("Lightspeed7"))
  }

  def cookbook = Action {
    Ok(views.html.cookbook("Cookbook"))
  }
}