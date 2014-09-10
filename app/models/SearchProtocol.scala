package models

import play.api.libs.json._
import java.util.UUID
import play.api.libs.iteratee.Enumerator
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.DateTime
import play.api.data.validation.ValidationError

case class Tick()
case class TickStart()
case class TickStop()

object CurrentTime {
  val timestampFormat = ISODateTimeFormat.dateTime()

  def now(): String = {

    timestampFormat.print(new DateTime(System.currentTimeMillis()))
  }
}

case class LogEntry(ts: DateTime, verb: String, device: String, agent: String, time: Int, path: String, status: Int)

case class SearchFeed(out: Enumerator[JsValue])

case class SearchMatch(logEntry: LogEntry, matchingChannelIds: List[UUID])

case class StartSearch(id: UUID = UUID.randomUUID(), searchString: String)

case class StopSearch(id: UUID)

case class Statistics(data: JsValue)

