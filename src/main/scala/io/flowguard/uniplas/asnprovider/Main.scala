package io.flowguard.uniplas.asnprovider

import io.flowguard.uniplas.asnprovider.GeoLiteProvider

import com.comcast.ip4s._

@main def run: Unit =
  // val test = Cidr.fromString("10.127.0.0/16")

  // val r1 = test.get.contains(Ipv4Address.fromString("10.128.0.1").get)
  // val r2 = test.get.contains(Ipv4Address.fromString("10.127.0.1").get)

  val t = new GeoLiteProvider(System.getenv("GEOLITE_KEY"))
  val r = t.load

  println("Hello world!")
  println(msg)

def msg = "I was compiled by Scala 3. :)"
