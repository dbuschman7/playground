package actors

import akka.actor.{ ActorRef, Actor }
import models.{ SearchMatch, StopSearch, LogEntry, StartSearch }
import play.api.libs.ws.WS
import play.api.libs.json.{ JsArray, JsValue, Json }
import java.util.UUID
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import java.util.Map
import models.ActionCounts
/**
 */
class ActionCountsActor extends Actor {

  val mainSearch = context.system.actorFor("/user/channelSearch")

  val get: Int = 1;
  val put: Int = 2;
  val post: Int = 4;
  val delete: Int = 3;

  def receive = {
    case LogEntry(data) => process(data, sender)
  }

  private def process(logJson: JsValue, requestor: ActorRef) {
    println(s"process called $logJson")
    // do something here

    // generate the return data
    mainSearch ! ActionCounts(get, put, post, delete)
  }

}
