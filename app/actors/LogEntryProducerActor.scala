package actors

import akka.actor.{ Actor }
import java.util.Random
import play.api.libs.json.Json
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import models.{ LogEntry, Tick }
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import models.CurrentTime

/**
 */
class LogEntryProducerActor extends Actor {

  val devices = Array("Desktop", "Tablet", "Phone", "TV")

  val userAgents = Array("Chrome", "Firefox", "IE", "Safari", "HttpClient")

  val paths = Array("/a", "/b", "/c", "/d", "/e")

  val methods = Array("GET", "POST", "PUT", "DELETE")

  val statuses = Array(200, 404, 201, 500)

  val searchStore = context.system.actorSelection("/user/elasticSearch")
  val actionCounts = context.system.actorSelection("/user/statistics")

  def receive = {
    case Tick(current) => {
      val entry = LogEntry(generateLogEntry)
      searchStore ! entry
      actionCounts ! entry
    }
  }

  override def preStart() {
    println("Log Generator Starting")
  }

  private def generateLogEntry = {
    Json.obj(
      "timestamp" -> CurrentTime.generateTick.time,
      "response_time" -> randomResponseTime,
      "method" -> randomElement(methods),
      "path" -> randomElement(paths),
      "status" -> randomElement(statuses),
      "device" -> randomElement(devices),
      "user_agent" -> randomElement(userAgents))
  }

  private def randomElement[A](list: Array[A]) = {
    val rand = new Random(System.currentTimeMillis())
    val randomIndex = rand.nextInt(list.length)
    list(randomIndex)
  }

  private def randomResponseTime = new Random(System.currentTimeMillis()).nextInt(1000)

}
