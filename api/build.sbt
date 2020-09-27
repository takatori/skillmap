name := "skillmap"
scalaVersion := "2.13.2"
version := "0.1.0-SNAPSHOT"
val zioVer        = "1.0.0"
val http4sVer     = "0.21.6"
val tapirVer      = "0.16.13"
val circeVer      = "0.13.0"
val catsEffectVer = "2.1.3"
val doobieVer     = "0.9.0"

val dependencies = Seq(
  "dev.zio"                     %% "zio"                      % zioVer,
  "dev.zio"                     %% "zio-interop-cats"         % "2.1.4.0",
  "org.typelevel"               %% "cats-effect"              % catsEffectVer,
  "org.http4s"                  %% "http4s-core"              % http4sVer,
  "org.http4s"                  %% "http4s-dsl"               % http4sVer,
  "org.http4s"                  %% "http4s-blaze-server"      % http4sVer,
  "org.http4s"                  %% "http4s-circe"             % http4sVer,
  "io.circe"                    %% "circe-core"               % circeVer,
  "io.circe"                    %% "circe-generic"            % circeVer,
  "io.circe"                    %% "circe-parser"             % circeVer,
  "org.tpolecat"                %% "doobie-core"              % doobieVer,
  "org.tpolecat"                %% "doobie-h2"                % doobieVer,
  "org.tpolecat"                %% "doobie-hikari"            % doobieVer,
  "com.softwaremill.sttp.tapir" %% "tapir-core"               % tapirVer,
  "com.softwaremill.sttp.tapir" %% "tapir-zio"                % tapirVer,
  "com.softwaremill.sttp.tapir" %% "tapir-zio-http4s-server"  % tapirVer,
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server"      % tapirVer,
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe"         % tapirVer,
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs"       % tapirVer,
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % tapirVer,
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-http4s"  % tapirVer,
  "mysql"                       % "mysql-connector-java"      % "8.0.21",
  "org.slf4j"                   % "slf4j-simple"              % "1.6.4"
)

val compileOptions = Seq(
  "-encoding",
  "utf8",
  "-Xfatal-warnings",
  "-Xlint",
  "-deprecation",
  "-unchecked",
  "-Wdead-code",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps"
)

lazy val root = (project in file("."))
  .settings(name := "skills")
  .settings(
    scalacOptions ++= compileOptions,
    libraryDependencies ++= dependencies ++ Seq()
  )
