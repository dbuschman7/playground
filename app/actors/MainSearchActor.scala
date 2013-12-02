package actors

import akka.actor.{ Props, Actor }
import play.api.libs.iteratee.{ Concurrent }
import models._
import scala.collection.mutable.HashMap
import java.util.UUID
import play.api.libs.json.JsValue
import scala.concurrent.duration._
import models.StartSearch
import models.SearchMatch
import models.StopSearch
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import play.api.libs.json.JsObject
import play.api.libs.json.JsObject
import scala.collection.immutable.Seq
import scala.collection.Seq
import play.api.libs.json.JsObject
import play.api.libs.json.Json

/**
 */
class MainSearchActor extends Actor {

  var channels = new HashMap[UUID, Concurrent.Channel[JsValue]]

  val elasticSearchActor = context.system.actorFor("/user/elasticSearch")

  def receive = {
    case startSearch: StartSearch => sender ! SearchFeed(startSearching(startSearch))
    case stopSearch: StopSearch => stopSearching(stopSearch)
    case searchMatch: SearchMatch => broadcastToClient(searchMatch)
    case actionCounts: ActionCounts => broadcastToClient(actionCounts)
  }

  private def broadcastToClient(searchMatch: SearchMatch) {
    println("Broadcasting SearchResult Match")
    searchMatch.matchingChannelIds.foreach {
      channels.get(_).map {
        val data: JsValue = searchMatch.logEntry.data
        val target: JsValue = Json.obj("target" -> "searchResult", "data" -> data)
        println(s"JSValue = $target")
        _ push target
      }
    }
  }

  private def broadcastToClient(actionCounts: ActionCounts) {
    println("Broadcasting ActionsCounts Match")
    channels.values map { ch =>
      val data: JsValue = actionCounts.toJson
      val target: JsValue = Json.obj("target" -> "actionCounts", "data" -> data)
      println(s"JSValue = $target")
      ch push target
    }
  }

  private def startSearching(startSearch: StartSearch) =
    Concurrent.unicast[JsValue](
      onStart = (c) => {
        channels += (startSearch.id -> c)
        elasticSearchActor ! startSearch
      },
      onComplete = {
        self ! StopSearch(startSearch.id)
      },
      onError = (str, in) => {
        self ! StopSearch(startSearch.id)
      }).onDoneEnumerating(
        callback = {
          self ! StopSearch(startSearch.id)
        })

  private def stopSearching(stopSearch: StopSearch) {
    channels -= stopSearch.id
    elasticSearchActor ! stopSearch
  }
}
