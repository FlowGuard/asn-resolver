val scala2Version = "2.13.7"

lazy val root = project
  .in(file("."))
  .settings(
    name := "uni-asn-provider",
    version := "0.1.0",

    scalaVersion := scala2Version,
    libraryDependencies += "com.comcast" %% "ip4s-core" % "3.0.3",
    libraryDependencies += "org.wvlet.airframe" %% "airframe-log" % "21.9.0",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % Test,
    libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.6.17" % Test
  )
enablePlugins(AkkaGrpcPlugin)

akkaGrpcGeneratedSources := Seq(AkkaGrpc.Server)
