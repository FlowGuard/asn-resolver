package io.flowguard.asnprovider

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object Main extends App {
  val conf = ConfigFactory
    .parseString("akka.http.server.preview.enable-http2 = on") // TODO load from application.conf
    .withFallback(ConfigFactory.defaultApplication())
  val system = ActorSystem("AsnProvider", conf)

  AsnProviderServer(system).run()
}
