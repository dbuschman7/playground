package actors

import akka.actor.{ ActorRef, Actor }
import models.{ SearchMatch, StopSearch, LogEntry, StartSearch }
import play.api.libs.ws._
import play.api.Play.current
import play.api.libs.json.{ JsArray, JsValue, Json }
import java.util.UUID
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import scala.concurrent.impl.Future
import scala.concurrent.Future
import play.api.libs.ws.ning.NingWSClient
import com.ning.http.client.AsyncHttpClientConfig
import scala.util.Success
import scala.util.Failure
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.DBCollection
import com.mongodb.DBObject
import com.mongodb.BasicDBObjectBuilder
import java.util.Date
import java.text.SimpleDateFormat
import org.joda.time.format.ISODateTimeFormat
import com.mongodb.Cursor
import com.mongodb.Bytes
import com.deftlabs.cursor.mongo.TailableCursorOptions
import actors.mongo.cursor.MongoCursorWrapped
import com.mongodb.DB
import play.Logger
import java.util.List
import java.util.ArrayList
import com.mongodb.BasicDBObject
import com.deftlabs.cursor.mongo.TailableCursorImpl
import models.TickStop
import scala.collection.mutable.Map
import play.api.libs.json.Format
import utils.MongoConfig

class TailableCursorActor extends Actor {

  val channels = context.system.actorSelection("/user/channels")

  var coll: DBCollection = _
  var options: TailableCursorOptions = new TailableCursorOptions("logs")

  val cursors = Map[UUID, MongoCursorWrapped]()

  def receive = {
    case log: LogEntry => percolate(log, sender)

    case StartSearch(id, searchString) => registerQuery(id, searchString)

    case StopSearch(id) => {
      println("StopSearch - received")
      unregisterQuery(id)
    }

    case TickStop => {
      println("TickStop received")
      cursors.keySet.foreach(uuid => unregisterQuery(uuid))
    }
  }

  private def percolate(log: LogEntry, requestor: ActorRef) {
    //    println(s"percolate called $logJson")

    //
    //    val action = (logJson \ "method").as[String]
    //    val device = (logJson \ "device").as[String]
    //    val agent = (logJson \ "agent").as[String]
    //    val responseTime = (logJson \ "time").as[Long]
    //    val path = (logJson \ "path").as[String]
    //    val status = (logJson \ "status").as[Int]

    //{"timestamp":"2014-09-06T23:04:57.713-06:00","response_time":342,"method":"DELETE","path":"/c","status":500,"device":"TV","user_agent":"IE"}
    //    val timestamp = ISODateTimeFormat.dateTime().parseDateTime((logJson \ "ts").as[String]);

    val obj = BasicDBObjectBuilder //
      .start("ts", log.ts.toDate()) //
      .add("verb", log.verb) //
      .add("device", log.device) //
      .add("agent", log.agent) //
      .add("time", log.time) //
      .add("path", log.path) //
      .add("status", log.status) //
      .get()

    coll.save(obj)

  }

  private def unregisterQuery(id: UUID) {
    println(s"MongoCollection - unregister query - id = $id")

    val removed = cursors.remove(id)
    if (removed.nonEmpty) {
      removed.get.stop
    }

  }

  private def registerQuery(id: UUID, searchString: String) {
    println(s"MongoCollection - register query - $id")

    // parse the query string 
    val localSearch = searchString.concat(" ")
    val params = localSearch.split(" ")
    val cursor = new MongoCursorWrapped(id, channels, params)
    try {
      cursor.start(MongoConfig.db, options)
      cursors.put(id, cursor)
    } catch {
      case e: Exception => {
        cursor.stop()
        Logger.error("Error trying to standup cursor", e)
      }
    }
  }

  override def preStart() {

    options.setDefaultCappedCollectionSize(100 * 100)
    options.setAssertIfNoCappedCollection(false)

    val impl = new TailableCursorImpl(MongoConfig.db, options) // causes the collection to be created
    coll = impl.getCollection()

  }
}