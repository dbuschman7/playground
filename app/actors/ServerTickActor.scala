package actors

import akka.actor.{ Actor }
import java.util.Random
import play.api.libs.json.Json
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import models.{ LogEntry, Tick, TickStart, TickStop }
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import models.CurrentTime
import java.util.concurrent.atomic.AtomicInteger

/**
 */
class ServerTickActor extends Actor {

  val mainSearch = context.system.actorSelection("/user/channelSearch")
  val producer = context.system.actorSelection("/user/logEntryProducerActor")

  val ticksPerClick: Int = 10

  var cancellable: akka.actor.Cancellable = null

  val remainingTicks: AtomicInteger = new AtomicInteger(0)

  def receive = {
    case Tick(current) => {
      println("Tick Generated")
      val currentTick = CurrentTime.generateTick;
      mainSearch ! currentTick;
      producer ! currentTick;

      if (0 == remainingTicks.decrementAndGet()) {
        self ! TickStop
      }
    }
    case TickStart => {
      println("Tick Start")
      remainingTicks.getAndAdd(ticksPerClick);
      if (cancellable == null) {
        cancellable = context.system.scheduler.schedule(0 second, 1 second, self, CurrentTime.generateTick)
      }
    }

    case TickStop => {
      println("Tick Stop")
      if (cancellable != null) {
        cancellable.cancel
        cancellable = null
        mainSearch ! TickStop
      }
    }
  }

  override def preStart() {
    println("Server Tick Ready")
  }

  override def postStop() {
    println("Server Tick - Shutting Down")
    if (cancellable != null) {
      cancellable.cancel
    }
    super.postStop
  }

}
