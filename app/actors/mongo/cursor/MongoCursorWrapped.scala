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
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.DateTime

class MongoCursorWrapped(id: UUID, sendMatch: ActorSelection, queryParams: Array[String]) extends TailableCursorDocListener(id.toString()) {

  def nextDoc(pDoc: DBObject) {

    // determine if row is a match
    val set = Set( //
      pDoc.get("verb").toString, //
      pDoc.get("path").toString, //
      pDoc.get("status").toString, //
      pDoc.get("device").toString, //
      pDoc.get("agent").toString //
      )

    // search all combinations of params and terms for any match
    val found: Boolean = queryParams.foldLeft(false)((acc, param) => acc | set.contains(param))
    if (!found) {
      return
    }

    // the answer is yes 
    val data = new LogEntry(
      new DateTime(pDoc.get("ts")),
      pDoc.get("verb").toString,
      pDoc.get("device").toString,
      pDoc.get("agent").toString,
      Integer.parseInt(pDoc.get("time").toString),
      pDoc.get("path").toString,
      Integer.parseInt(pDoc.get("status").toString) //
      )

    sendMatch ! new SearchMatch(data, List(id))
  }
}