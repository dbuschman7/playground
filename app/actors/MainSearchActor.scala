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
    case statistics: Statistics => broadcastToClient(statistics)
    case tick: Tick => broadcastToClient(tick)
  }

  // broadcast to all open channels
  private def broadcastToClient(tick: Tick) {
    // println("Broadcasting Server Tick")
    channels.values map { ch =>
      val data: JsValue = Json.obj("target" -> "serverTick", "data" -> tick.time)
      // println(s"JSValue = $data")
      ch push data
    }
  }

  // send to the specific channels that want this data.
  private def broadcastToClient(searchMatch: SearchMatch) {
    // println("Broadcasting SearchResult Match")
    searchMatch.matchingChannelIds.foreach {
      channels.get(_).map {
        val data: JsValue = Json.obj("target" -> "searchResult", "data" -> searchMatch.logEntry.data)
        // println(s"JSValue = $data")
        _ push data
      }
    }
  }

  // broascast to all open channels
  private def broadcastToClient(statistics: Statistics) {
    // println("Broadcasting Statistics Match")
    channels.values map { ch =>
      val data: JsValue = Json.obj("target" -> "statistics", "data" -> statistics.data)
      // println(s"JSValue = $data")
      ch push data
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
