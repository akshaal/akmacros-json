import sbt._
import Keys._

import org.ensime.sbt.Plugin.Settings.ensimeConfig
import org.ensime.sbt.util.SExp._

object build extends Build {
    val sharedSettings = Defaults.defaultSettings ++ Seq(
        organization := "info.akshaal",
        version := "0.1-SNAPSHOT",
        scalaVersion := "2.10.0-M7",
        scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:higherKinds", "-language:implicitConversions"),

        resolvers ++= Seq("snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
                          "releases"  at "http://oss.sonatype.org/content/repositories/releases",
                          "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"),

        libraryDependencies <<= scalaVersion { scala_version =>
            Seq(
                "info.akshaal" %% "macros" % "0.3",
                "com.codahale" %% "jerkson" % "0.6.0-SNAPSHOT",

                // Test dependencies
                "org.specs2" %% "specs2" % "1.12.1.1" % "test" cross CrossVersion.full,
                "org.scalacheck" % "scalacheck" % "1.10.0" % "test" cross CrossVersion.full
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
        id = "json",
        base = file("."),
        settings = sharedSettings
    )
}
