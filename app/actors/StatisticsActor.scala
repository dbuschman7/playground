package actors

import akka.actor.{ ActorRef, Actor }
import models.{ SearchMatch, StopSearch, LogEntry, StartSearch }
import play.api.libs.ws.WS
import play.api.libs.json.{ JsArray, JsValue, Json }
import java.util.UUID
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import models.Statistics
import java.util.HashMap
import java.util.concurrent.atomic.AtomicInteger
import play.api.libs.json.JsArray
import java.util.Map
import scala.collection.JavaConversions._
import play.api.libs.json.JsObject
import play.api.libs.json.JsObject
import play.api.libs.json.JsObject
import play.api.libs.json.JsObject
import java.util.concurrent.atomic.AtomicLong

/**
 */
class StatisticsActor extends Actor {

  val mainSearch = context.system.actorFor("/user/channelSearch")

  val data: Map[String, AtomicLong] = new HashMap;

  def receive = {
    case LogEntry(data) => process(data, sender)
  }

  def incValue(key: String) {
    if (data.get(key) == null) {
      data.put(key, new AtomicLong(1))
    } else {
      data.get(key).incrementAndGet()
    }
  }

  def sumValue(key: String, inc: Long) {
    if (data.get(key) == null) {
      data.put(key, new AtomicLong(inc))
    } else {
      data.get(key).addAndGet(inc)
    }
  }

  def getValue(key: String): Long = {
    if (data.get(key) == null) {
      0
    } else {
      data.get(key).longValue()
    }
  }

  private def process(logJson: JsValue, requestor: ActorRef) {
    //   println(s"process called $logJson")
    //
    var action = (logJson \ "method").as[String]
    var device = (logJson \ "device").as[String]
    var agent = (logJson \ "user_agent").as[String]
    var responseTime = (logJson \ "response_time").as[Long]

    //    println(s"Action = $action, Device = $device, Agent = $agent")
    incValue(action)
    incValue(device)
    incValue(agent)
    incValue("requests")
    sumValue("totalResponseTime", responseTime)

    // generate the return data
    // not the best here, just need something to work quickly
    val retVal =
      Json.obj(
        // methods
        "GET" -> getValue("GET"),
        "PUT" -> getValue("PUT"),
        "POST" -> getValue("POST"),
        "DELETE" -> getValue("DELETE"),

        // devices
        "Desktop" -> getValue("Desktop"),
        "Tablet" -> getValue("Tablet"),
        "Phone" -> getValue("Phone"),
        "TV" -> getValue("TV"),

        // agent
        "Chrome" -> getValue("Chrome"),
        "Firefox" -> getValue("Firefox"),
        "IE" -> getValue("IE"),
        "Safari" -> getValue("Safari"),
        "HttpClient" -> getValue("HttpClient"),

        // basic counts
        "requests" -> getValue("requests"),
        "totalResponseTime" -> getValue("totalResponseTime"));

    // println("Stringify = " + Json.stringify(retVal))
    mainSearch ! Statistics(retVal)
  }

}
