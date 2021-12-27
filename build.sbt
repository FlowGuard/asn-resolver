enablePlugins(AkkaGrpcPlugin)
enablePlugins(DockerPlugin)
enablePlugins(JavaAppPackaging)

val scala2Version = "2.13.7"
val akkaVersion = "2.6.9" // must be compatible with scala grpc plugin

lazy val root = project
  .in(file("."))
  .settings(
    name := "fg-asn-resolver",
    maintainer := "Jakub Pravda <jakub.pravda@comsource.cz>",
    version := "0.1.0",

    scalaVersion := scala2Version,
    libraryDependencies += "com.comcast" %% "ip4s-core" % "3.0.3",
    libraryDependencies += "org.wvlet.airframe" %% "airframe-log" % "21.9.0",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % Test,
    libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
  )

akkaGrpcGeneratedSources := Seq(AkkaGrpc.Server)

ThisBuild / scapegoatVersion := "1.4.11"

// docker settings
dockerUpdateLatest := true
packageName := "asn-resolver"
dockerRepository := Some("ghcr.io")
dockerUsername := Some("flowguard")
dockerExposedPorts := Seq(8090)

// test settings
Test / fork := true
Test / envVars := Map("ASN_PROVIDER" -> "dummy", "ASN_DB_REFRESH_RATE" -> "1 hour")
