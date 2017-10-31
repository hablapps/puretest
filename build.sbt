lazy val commonSettings = Seq(
  organization := "org.hablapps",
  version := "0.3.1-SNAPSHOT",
  scalaVersion := "2.12.3",
  crossScalaVersions := Seq("2.11.8", "2.12.3"),
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3"),
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.0.0",
    "org.scalacheck" %% "scalacheck" % "1.13.4",
    "com.lihaoyi" %% "sourcecode" % "0.1.3"),
  resolvers ++= Seq(
    "Speech repo - releases" at "http://repo.hablapps.com/releases",
    Resolver.sonatypeRepo("snapshots")),
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
  },
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-Ypartial-unification",
    "-Ywarn-unused-import",
    // "-Xprint:typer",
    // "-Xlog-implicit-conversions",
    "-feature",
    "-language:implicitConversions",
    "-language:postfixOps",
    "-language:higherKinds"),
  scalacOptions in (Compile, console) ~= (_ filterNot (_ == "-Ywarn-unused-import")),
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value
)


lazy val root = (project in file("."))
  .aggregate(cats, scalaz, tictactoe)
  .settings(
    commonSettings,
    publish := { },
    publishLocal := { })

lazy val cats = project
  .settings(
    commonSettings,
    name := "puretest-cats",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats" % "0.9.0"
    ))

lazy val scalaz = project
  .settings(
    commonSettings,
    name := "puretest-scalaz",
    libraryDependencies ++= Seq(
      "org.scalaz" %% "scalaz-core" % "7.2.9"
    ))

lazy val tictactoe = (project in file("examples/tictactoe"))
  .dependsOn(cats)
  .settings(
    commonSettings,
    name := "tictactoe-example",
    publish := { },
    publishLocal := { },
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % "0.18.0-SNAPSHOT",
      "org.http4s" %% "http4s-blaze-server" % "0.18.0-SNAPSHOT",
      "org.http4s" %% "http4s-blaze-client" % "0.18.0-SNAPSHOT",
      "org.http4s" %% "http4s-circe" % "0.18.0-SNAPSHOT",
      "io.circe" %% "circe-generic" % "0.9.0-M1",
      "io.circe" %% "circe-literal" % "0.9.0-M1"))
