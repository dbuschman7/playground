import java.io.File
import org.apache.commons.io.FileUtils
import play.api._
import utils.EmbeddedESServer
import play.libs.Akka
import akka.actor.Props
import actors.LogEntryProducerActor
import actors.ElasticsearchActor
import actors.UserChannelsActor
import actors.StatisticsActor
import actors.ServerTickActor
import actors.TailableCursorActor

object Global extends GlobalSettings {

  override def onStart(app: Application) {

    Akka.system.actorOf(Props[TailableCursorActor], "search")
    Akka.system.actorOf(Props[LogEntryProducerActor], "logEntryProducer")
    Akka.system.actorOf(Props[UserChannelsActor], "channels")
    Akka.system.actorOf(Props[StatisticsActor], "statistics")
    Akka.system.actorOf(Props[ServerTickActor], "serverTick")

  }

  override def onStop(app: Application) {
    //
  }
}