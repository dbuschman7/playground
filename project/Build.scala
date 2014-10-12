import sbt._
import Keys._
import sbt.Keys._
import java.io.PrintWriter
import java.io.File
import play.Play.autoImport._
import PlayKeys._
import sys.process.stringSeqToProcess
import sbtbuildinfo.Plugin._

import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._


object ApplicationBuild extends Build {

    
  val appName         = "playground"

  val branch = ""; // "git rev-parse --abbrev-ref HEAD".!!.trim
  val commit = ""; // "git rev-parse --short HEAD".!!.trim
  val buildTime = (new java.text.SimpleDateFormat("yyyyMMdd-HHmmss")).format(new java.util.Date())

  val major = 1
  val minor = 1
  val patch = 0
  val appVersion = s"$major.$minor.$patch-$commit"

  val scalaVersion = scala.util.Properties.versionString.substring(8) 


  println()
  println(s"App Name      => ${appName}")
  println(s"App Version   => ${appVersion}")
  println(s"Git Branch    => ${branch}")
  println(s"Git Commit    => ${commit}")
  println(s"Scala Version => ${scalaVersion}")
  println()
  
  val scalaBuildOptions = Seq("-unchecked", "-feature", "-language:reflectiveCalls", "-deprecation",
    "-language:implicitConversions", "-language:postfixOps", "-language:dynamics", "-language:higherKinds",
    "-language:existentials", "-language:experimental.macros", "-Xmax-classfile-name", "140")


    
    
  val appDependencies = Seq( ws,
//    "org.elasticsearch" % "elasticsearch" % "0.90.1",
    "commons-io" % "commons-io" % "2.4",
    "org.webjars" %% "webjars-play" % "2.3.0" withSources() ,
    "org.webjars" % "angularjs" % "1.2.23",
    "org.webjars" % "bootstrap" % "3.2.0",
    "org.webjars" % "d3js" % "3.4.11",
    "me.lightspeed7" % "mongoFS" % "0.8.1"
  )

  val playground = Project("playground", file("."))
    .enablePlugins(play.PlayScala)
    .settings(scalacOptions ++= scalaBuildOptions)
    .settings(
        version := appVersion,
        libraryDependencies ++= appDependencies
    )
    .settings( buildInfoSettings: _*)
    .settings( 
            maintainer := "David Buschman", // setting a maintainer which is used for all packaging types
            dockerExposedPorts in Docker := Seq(9000, 9443), // exposing the play ports
            dockerBaseImage := "play_java_mongo_db/latest",
            dockerRepository := Some("docker.transzap.com:2375/play_java_mongo_db")
    )
    .settings(
      sourceGenerators in Compile <+= buildInfo,
      buildInfoPackage := "me.lightspeed7.version",
      buildInfoKeys ++= Seq[BuildInfoKey] (
        "builtAt" -> {
          val dtf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
          dtf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"))
          dtf.format(new java.util.Date())
        },
        "builtAtMillis" -> { System.currentTimeMillis() },
        "major" -> { major },
        "minor" -> { minor },
        "patch" -> { patch },
        "commit" -> { commit }
        )
      )    
    .settings(
      resolvers += "MongoFS Interim Maven Repo" at "https://github.com/dbuschman7/mvn-repo/raw/master"
    )
    
   println(s"Deploy this with: docker run -p 10000:9000 ${appName}:${appVersion}")    

}
