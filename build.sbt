name := "puretest"

scalaVersion := "2.12.0"

scalaOrganization := "org.typelevel"

// scalaBinaryVersion := "2.12"

crossScalaVersions := Seq("2.11.8", "2.12.0")

organization := "org.hablapps"

version := "0.1"

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3")

addCompilerPlugin("org.scalamacros" %% "paradise" % "2.1.0" cross CrossVersion.full)

resolvers ++= Seq(
  "Speech repo - releases" at "http://repo.hablapps.com/releases")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.0",
  // "org.scalaz" %% "scalaz-core" % "7.3.0-M4-HABLAPPS",
  "org.scalaz" %% "scalaz-core" % "7.2.9",
  "org.typelevel" %% "cats" % "0.9.0",
  "com.lihaoyi" %% "sourcecode" % "0.1.3"
)

publishTo <<= version { v =>
  import java.io.File
  val privateKeyFile: File = new File(sys.env("HOME") + "/.ssh/hablaweb.pem")
  Some(Resolver.sftp(
    "HABLA",
    "repo.hablapps.com",
    "/var/www/repo/html/" + (
      if (v.trim.endsWith("SNAPSHOT")) { "snapshots" } else { "releases" }
    )
  ) as("ubuntu", privateKeyFile))
}

// dependencyOverrides += "org.scalaz" %% "scalaz-core" % "7.2.7-HABLAPPS"

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Ypartial-unification",
  // "-Xprint:typer",
  // "-Xlog-implicit-conversions",
  "-feature",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-language:higherKinds")