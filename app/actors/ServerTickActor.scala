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
class ServerTickActor extends Actor {

  val mainSearch = context.system.actorFor("/user/channelSearch")
  val producer = context.system.actorFor("/user/logEntryProducerActor")

  val cancellable = context.system.scheduler.schedule(0 second, 1 second, self, CurrentTime.generateTick)

  def receive = {
    case Tick(current) => {
      //      println("Tick generated")
      val currentTick = CurrentTime.generateTick;
      mainSearch ! currentTick;
      producer ! currentTick;
    }
  }

  override def preStart() {
    println("Server Tick Starting")

  }

  override def postStop() {
    cancellable.cancel
    super.postStop
  }

}
