name := "wiremock-poc"

version := "0.1"

scalaVersion := "2.13.2"

val akkaV     = "2.6.1"
val akkaHttpV = "10.1.11"

libraryDependencies ++= Seq(
  // http client stuff
  "com.typesafe.akka" %% "akka-http"   % akkaHttpV,
  "com.typesafe.akka" %% "akka-stream" % akkaV,
  // json serialization util
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV,
  "io.spray"          %% "spray-json"           % "1.3.5",
  // testing
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV % Test,
  "com.typesafe.akka" %% "akka-testkit"      % akkaV     % Test,
  "org.mockito"       %% "mockito-scala"     % "1.13.1"  % Test,
  "org.scalatest"     %% "scalatest"         % "3.1.1"   % Test,
  // wiremock
  "com.github.tomakehurst" % "wiremock" % "2.27.0" % Test,
)
