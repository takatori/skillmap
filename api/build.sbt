name := "skillmap"
scalaVersion     := "2.13.2"
version          := "0.1.0-SNAPSHOT"


val zioVer        = "1.0.0"
val http4sVer     = "0.21.6"
val tapirVer      = "0.16.10"
val circeVer      = "0.13.0"
val catsEffectVer = "2.1.3"

val commonLib = Seq(
  "dev.zio" %% "zio" % zioVer,
  "dev.zio" %% "zio-interop-cats" % "2.1.4.0",
  "org.typelevel" %% "cats-effect"         % catsEffectVer,
  "org.http4s"    %% "http4s-core"         % http4sVer,
  "org.http4s"    %% "http4s-dsl"          % http4sVer,
  "org.http4s"    %% "http4s-blaze-server" % http4sVer,
  "org.http4s"    %% "http4s-circe"        % http4sVer,
  "org.slf4j"     %  "slf4j-simple"        % "1.6.4"
)

val circeLib = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVer)


val compileOptions = Seq(
  "-encoding", "utf8",
  "-Xfatal-warnings",
  "-deprecation",
  "-unchecked",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps"
)

lazy val root = (project in file("."))
  .settings(name := "skills")
  .settings(
    scalacOptions ++= compileOptions,
    libraryDependencies ++= commonLib ++ Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-core"          % tapirVer,
      "com.softwaremill.sttp.tapir" %% "tapir-zio" % tapirVer,
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http4s-server" % tapirVer,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVer,
    )
  )

