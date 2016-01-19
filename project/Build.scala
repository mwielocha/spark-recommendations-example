import sbt.Tests.Setup
import sbt._
import sbt.Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

object AppBuild extends Build {

  val appName         = "spark-recommendations"
  val appVersion      = "1.0.0"

  object V {

    val play = "2.3.10"
    val spark = "1.6.0"

  }

  val appDependencies = Seq(
    "org.apache.spark"  %% "spark-core" % V.spark withSources(),
    "joda-time"         % "joda-time"   % "2.9.1"
  )

  val buildSettings = Seq(
    resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
    resolvers += Resolver.mavenLocal,
    version := appVersion,
    organization := "io.cyberdolphins",
    fork := true,
    exportJars := true,
    scalaVersion := "2.11.6",
    test in assembly := { },
    testOptions in Test += Tests.Argument("-oFD"),
    mainClass in (Compile, run) := Some("io.cyberdolphins.recommendations.Crunch"),
    artifact in (Compile, assembly) := {
      val art = (artifact in (Compile, assembly)).value
      art.copy(`classifier` = Some("assembly"))
    }
  ) ++ addArtifact(artifact in (Compile, assembly), assembly)

  addArtifact(artifact in (Compile, assembly), assembly)

  val main = Project(id = appName, base = file("."),
    settings = assemblySettings ++ buildSettings ++ Seq(
      libraryDependencies ++= appDependencies
    ) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings
  )
}
