package io.flowguard.asnprovider

import akka.actor.ActorSystem
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}

object Main extends App {
  val conf = ConfigFactory.load()
    .withValue("akka.http.server.preview.enable-http2", ConfigValueFactory.fromAnyRef("on"))
    .withFallback(ConfigFactory.defaultApplication())
  val system = ActorSystem("AsnProvider", conf)

  AsnProviderServer(system).run()
}

// TODO add readme
