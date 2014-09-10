package actors

import akka.actor.{ Actor }
import java.util.Random
import play.api.libs.json.Json
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import models.{ LogEntry, Tick, TickStop, CurrentTime }
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

/**
 */
class LogEntryProducerActor extends Actor {

  val devices = Array("Desktop", "Tablet", "Phone", "TV")

  val userAgents = Array("Chrome", "Firefox", "IE", "Safari", "HttpClient")

  val paths = Array("/a", "/b", "/c", "/d", "/e")

  val verbs = Array("GET", "POST", "PUT", "DELETE")

  val statuses = Array(200, 404, 201, 500)

  val searchStore = context.system.actorSelection("/user/search")
  val statistics = context.system.actorSelection("/user/statistics")

  def receive = {
    case t: Tick => {
      val entry = generateLogEntry
      searchStore ! entry
      statistics ! entry
    }
    case TickStop => {
      searchStore ! TickStop
      statistics ! TickStop
    }
  }

  override def preStart() {
    println("Log Generator Starting")
  }

  private def generateLogEntry: LogEntry = {

    return new LogEntry(
      DateTime.now(),
      randomElement(verbs),
      randomElement(devices),
      randomElement(userAgents),
      randomResponseTime,
      randomElement(paths),
      randomElement(statuses))
  }

  private def randomElement[A](list: Array[A]) = {
    val rand = new Random(System.currentTimeMillis())
    val randomIndex = rand.nextInt(list.length)
    list(randomIndex)
  }

  private def randomResponseTime = new Random(System.currentTimeMillis()).nextInt(1000)

}
