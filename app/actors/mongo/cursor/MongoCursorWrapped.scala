package actors.mongo.cursor

import com.deftlabs.cursor.mongo.TailableCursorOptions
import com.deftlabs.cursor.mongo.TailableCursor
import akka.actor.ActorRef
import com.deftlabs.cursor.mongo.TailableCursorDocListener
import com.mongodb.DBObject
import models.SearchMatch
import models.SearchMatch
import play.api.libs.json.Json
import models.LogEntry
import java.util.UUID
import com.deftlabs.cursor.mongo.TailableCursorImpl
import com.mongodb.DB
import akka.actor.ActorSelection

class MongoCursorWrapped(id: UUID, sendMatch: ActorSelection, queryParams: Array[String]) extends TailableCursorDocListener {

  var cursor: TailableCursor = _;

  def start(db: DB, options: TailableCursorOptions) {

    // make sure I am my own listener
    options.setDocListener(this)
    options.setThreadName(id.toString())

    // fire up the cursor
    cursor = new TailableCursorImpl(db, options)
    cursor.start()
  }

  def stop() {
    if (cursor == null) return

    if (cursor.isRunning()) {
      cursor.stop()
    }
  }

  def nextDoc(pDoc: DBObject) {

    // determine if row is a match
    val set = Set( //
      pDoc.get("method").toString, //
      pDoc.get("path").toString, //
      pDoc.get("status").toString, //
      pDoc.get("device").toString, //
      pDoc.get("agent").toString //
      )

    val found: Boolean = queryParams.foldLeft(false)((acc, param) => acc | set.contains(param))
    if (!found) {
      return
    }

    // the answer is yes 
    val data = Json.obj(
      "ts" -> pDoc.get("ts").toString,
      "time" -> pDoc.get("time").toString,
      "method" -> pDoc.get("method").toString,
      "path" -> pDoc.get("path").toString,
      "status" -> pDoc.get("status").toString,
      "device" -> pDoc.get("device").toString,
      "agent" -> pDoc.get("agent").toString)

    sendMatch ! new SearchMatch(LogEntry(data), List(id))
  }
}