package io.flowguard.uniplas.asnprovider

import io.flowguard.uniplas.asnprovider.GeoLiteProvider

import com.comcast.ip4s._

@main def run: Unit =
  val t = new GeoLiteProvider(System.getenv("GEOLITE_KEY"))
  val r = t.load
