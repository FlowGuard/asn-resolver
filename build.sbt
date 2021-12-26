enablePlugins(AkkaGrpcPlugin)
enablePlugins(DockerPlugin)
enablePlugins(JavaAppPackaging)

val scala2Version = "2.13.7"
val akkaVersion = "2.6.9" // must be compatible with scala grpc plugin

lazy val root = project
  .in(file("."))
  .settings(
    name := "fg-asn-provider",
    maintainer := "Jakub Pravda <jakub.pravda@comsource.cz>",
    version := "0.0.1-SNAPSHOT",

    scalaVersion := scala2Version,
    libraryDependencies += "com.comcast" %% "ip4s-core" % "3.0.3",
    libraryDependencies += "org.wvlet.airframe" %% "airframe-log" % "21.9.0",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % Test,
    libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
  )

akkaGrpcGeneratedSources := Seq(AkkaGrpc.Server)

ThisBuild / scapegoatVersion := "1.4.11"

Docker / packageName := "fg-asn-service"
