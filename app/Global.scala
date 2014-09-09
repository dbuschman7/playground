import java.io.File
import org.apache.commons.io.FileUtils
import play.api._
import utils.EmbeddedESServer
import play.libs.Akka
import akka.actor.Props
import actors.LogEntryProducerActor
import actors.ElasticsearchActor
import actors.MainSearchActor
import actors.StatisticsActor
import actors.ServerTickActor
import actors.TailableCursorActor

object Global extends GlobalSettings {
  //
  //  var esServer: EmbeddedESServer = _
  //
  //  var esDataDirectory: File = _
  //  val elastic = false;

  override def onStart(app: Application) {

    //    if (elastic) {
    //      esDataDirectory = new File(app.path, "elasticsearch-data")
    //      FileUtils.deleteDirectory(esDataDirectory)
    //      esServer = new EmbeddedESServer(esDataDirectory)
    //      esServer.client.admin.indices.prepareCreate("logentries").execute().get
    //
    //      Akka.system.actorOf(Props[ElasticsearchActor], "search")
    //    } else {

    Akka.system.actorOf(Props[TailableCursorActor], "search")
    //    }

    // create the single instance actors
    Akka.system.actorOf(Props[LogEntryProducerActor], "logEntryProducer")
    Akka.system.actorOf(Props[MainSearchActor], "channels")
    Akka.system.actorOf(Props[StatisticsActor], "statistics")
    Akka.system.actorOf(Props[ServerTickActor], "serverTick")

  }

  override def onStop(app: Application) {
    //    esServer.shutdown
    //    FileUtils.deleteDirectory(esDataDirectory)
  }
}