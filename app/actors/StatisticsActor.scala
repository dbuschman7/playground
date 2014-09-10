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
import models.TickStop

/**
 */
class StatisticsActor extends Actor {

  val channels = context.system.actorSelection("/user/channels")

  val data: Map[String, AtomicLong] = new HashMap;

  def receive = {
    case log: LogEntry => {
      //      println("Processing LogEntry")
      process(log, sender)
      updateUserChannels
    }
    case TickStop => {
      data.clear() // reset everything to zeros
    }
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

  private def process(log: LogEntry, requestor: ActorRef) {
    //   println(s"process called $logJson")

    incValue(log.verb)
    incValue(log.device)
    incValue(log.agent)
    incValue("requests")
    sumValue("totalResponseTime", log.time)

  }

  private def updateUserChannels() {
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
    channels ! Statistics(retVal)
  }
}
