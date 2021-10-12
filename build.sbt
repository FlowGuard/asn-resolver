val scala3Version = "3.0.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "uni-asn-provider",
    version := "0.1.0",

    scalaVersion := scala3Version,
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % "test",
    libraryDependencies += "com.comcast" %% "ip4s-core" % "3.0.3",
    libraryDependencies += "org.wvlet.airframe" %% "airframe-log" % "21.9.0"
  )
enablePlugins(AkkaGrpcPlugin)
