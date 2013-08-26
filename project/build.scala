import sbt._
import Keys._

import org.ensime.sbt.Plugin.Settings.ensimeConfig
import org.ensime.sbt.util.SExp._

object build extends Build {
    val sharedSettings = Defaults.defaultSettings ++ Seq(
        organization := "info.akshaal",
        version := "0.1-SNAPSHOT",
        scalaVersion := "2.10.2",
        scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:higherKinds", "-language:implicitConversions"),

        resolvers ++= Seq("snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
                          "releases"  at "http://oss.sonatype.org/content/repositories/releases",
                          "Mandubian repository snapshots" at "https://github.com/mandubian/mandubian-mvn/raw/master/snapshots/",
                          "Mandubian repository releases" at "https://github.com/mandubian/mandubian-mvn/raw/master/releases/",
                          "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"),

        libraryDependencies <<= scalaVersion { scala_version =>
            Seq(
                "org.scala-lang" % "scala-reflect" % scala_version % "provided",
                //"info.akshaal" %% "macros" % "0.3",
                //"com.codahale" %% "jerkson" % "0.6.0-SNAPSHOT",
                "play" %% "play-json" % "2.2-SNAPSHOT",

                // Test dependencies
                "org.specs2" %% "specs2" % "2.1.1" % "test",
                "org.scalacheck" %% "scalacheck" % "1.10.1" % "test"
            )
        },

        ensimeConfig := sexp(
            key(":compiler-args"), sexp("-Ywarn-dead-code", "-Ywarn-shadowing"),
            key(":formatting-prefs"), sexp(
                key(":alignParameters"), true,
                key(":alignSingleLineCaseStatements"), true,
                key(":compactControlReadability"), true,
                key(":doubleIndentClassDeclaration"), true,
                key(":preserveDanglingCloseParenthesis"), true,
                key(":indentSpaces"), 4
            )
        ))

    lazy val root = Project(
        id = "akmacros-json",
        base = file("."),
        settings = sharedSettings
    ) dependsOn(fixedMacros)

    lazy val fixedMacros = RootProject(uri("https://github.com/ephe-meral/akmacros.git#fix-scala-2.10.2"))

}
