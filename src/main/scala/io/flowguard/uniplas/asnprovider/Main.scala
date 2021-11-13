package io.flowguard.uniplas.asnprovider

import akka.actor.ActorSystem
import io.flowguard.uniplas.asnprovider.GeoLiteProvider
import com.comcast.ip4s.*
import com.typesafe.config.ConfigFactory

@main def run: Unit =
  val conf = ConfigFactory
    .parseString("akka.http.server.preview.enable-http2 = on")
    .withFallback(ConfigFactory.defaultApplication())
  val system = ActorSystem("AsnProvider", conf)

  AsnProviderServer(system).run()

//  val t = new GeoLiteProvider(System.getenv("GEOLITE_KEY"))
//  val r = t.load
//
//  println(r.searchByIpAddress(ip"86.49.247.199"))
//  println("another")
//  println(r.searchByAutonomousNumber("16019"))
