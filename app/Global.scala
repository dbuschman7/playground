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

object Global extends GlobalSettings {

  var esServer: EmbeddedESServer = _

  var esDataDirectory: File = _

  override def onStart(app: Application) {
    esDataDirectory = new File(app.path, "elasticsearch-data")
    FileUtils.deleteDirectory(esDataDirectory)
    esServer = new EmbeddedESServer(esDataDirectory)
    esServer.client.admin.indices.prepareCreate("logentries").execute().get

    // create the single instance actors
    Akka.system.actorOf(Props[LogEntryProducerActor], "logEntryProducerActor")
    Akka.system.actorOf(Props[ElasticsearchActor], "elasticSearch")
    Akka.system.actorOf(Props[MainSearchActor], "channelSearch")
    Akka.system.actorOf(Props[StatisticsActor], "statistics")
    Akka.system.actorOf(Props[ServerTickActor], "serverTick")

  }

  override def onStop(app: Application) {
    esServer.shutdown
    FileUtils.deleteDirectory(esDataDirectory)
  }
}