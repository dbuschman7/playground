package models

import play.api.libs.json.{ Json, JsValue }
import java.util.UUID
import play.api.libs.iteratee.Enumerator
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.DateTime
import play.api.libs.json.JsArray
import play.api.libs.json.JsObject

case class Tick(time: String)
case class TickStart
case class TickStop

object CurrentTime {
  val timestampFormat = ISODateTimeFormat.dateTime()

  def generateTick(): Tick = {
    Tick(timestampFormat.print(new DateTime(System.currentTimeMillis())))
  }
}

case class LogEntry(data: JsValue) {
  def stringify = Json.stringify(data)
}

case class SearchFeed(out: Enumerator[JsValue])

case class SearchMatch(logEntry: LogEntry, matchingChannelIds: List[UUID])

case class StartSearch(id: UUID = UUID.randomUUID(), searchString: String)

case class StopSearch(id: UUID)

case class Statistics(data: JsValue)
