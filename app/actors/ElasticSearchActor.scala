package actors

import akka.actor.{ ActorRef, Actor }
import models.{ SearchMatch, StopSearch, LogEntry, StartSearch }
import play.api.libs.ws._;
import play.api.Play.current
import play.api.libs.json.{ JsArray, JsValue, Json }
import java.util.UUID
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
/**
 */
class ElasticsearchActor extends Actor {

  val mainSearch = context.system.actorSelection("/user/channelSearch")

  def receive = {
    case LogEntry(data) => percolate(data, sender)
    case StartSearch(id, searchString) => registerQuery(id, searchString)
    case StopSearch(id) => unregisterQuery(id)
  }

  private def percolate(logJson: JsValue, requestor: ActorRef) {
    //    println(s"percolate called $logJson")

    WS.url("http://localhost:9200/logentries/logentry/_percolate").post(Json.stringify(Json.obj("doc" -> logJson))).map {
      response =>
        val body = response.json
        val status = (body \ "ok").as[Boolean]
        if (status) {
          val matchingIds = (body \ "matches").asInstanceOf[JsArray].value.foldLeft(List[UUID]())((acc, v) => UUID.fromString(v.as[String]) :: acc)
          if (!matchingIds.isEmpty) {
            mainSearch ! SearchMatch(LogEntry(logJson), matchingIds)
          }
        }
    }
  }

  private def unregisterQuery(id: UUID) {
    //    println("unregister query")

    WS.url("http://localhost:9200/_percolator/logentries/" + id.toString).delete
  }

  private def registerQuery(id: UUID, searchString: String) {
    //    println("register query")

    val query = Json.obj(
      "query" -> Json.obj(
        "query_string" -> Json.obj(
          "query" -> searchString)))

    WS.url("http://localhost:9200/_percolator/logentries/" + id.toString).put(Json.stringify(query))
  }
}
