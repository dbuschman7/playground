import sbt._
import Keys._
import sbt.Keys._
import java.io.PrintWriter
import java.io.File
import play.Play.autoImport._
import PlayKeys._
import sys.process.stringSeqToProcess
import sbtbuildinfo.Plugin._

object ApplicationBuild extends Build {

  scalaVersion := "2.11.1"
    
  val appName         = "realtime-search"

  val branch = "git rev-parse --abbrev-ref HEAD".!!.trim
  val commit = "git rev-parse --short HEAD".!!.trim
  val buildTime = (new java.text.SimpleDateFormat("yyyyMMdd-HHmmss")).format(new java.util.Date())

  val major = 4
  val minor = 10
  val patch = 0
  val appVersion = s"$major.$minor.$patch-$commit"

  //val oildexVersion = "4.10.0-SNAPSHOT"
  val oildexVersion = "4.10.0-RC1"

  println()
  println(s"Git Branch    => ${branch}")
  println(s"Git Commit    => ${commit}")
  println(s"App Name      => ${appName}")
  println(s"App Version   => ${appVersion}")
  println(s"Scala Version => ${scalaVersion}")
  println()

  
  val scalaBuildOptions = Seq("-unchecked", "-feature", "-language:reflectiveCalls", "-deprecation",
    "-language:implicitConversions", "-language:postfixOps", "-language:dynamics", "-language:higherKinds",
    "-language:existentials", "-language:experimental.macros", "-Xmax-classfile-name", "140")


  val appDependencies = Seq( ws,
    "org.elasticsearch" % "elasticsearch" % "1.3.2",
    "commons-io" % "commons-io" % "2.4",
    "org.webjars" %% "webjars-play" % "2.3.0",
    "org.webjars" % "angularjs" % "1.2.23",
    "org.webjars" % "bootstrap" % "3.2.0"
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
        "commit" -> { commit },
        "core" -> { oildexVersion }
        )
      )    
    .settings(
      resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    )
}
