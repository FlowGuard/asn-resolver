package io.flowguard.asnprovider.providers

import io.flowguard.asnprovider.models.AsnDatabase

object Implicits {

  implicit val dummyAsnProvider: AsnProvider = new AsnProvider {
    override def load: AsnDatabase = AsnDatabase.empty()
  }

  implicit val geoLiteProvider: AsnProvider = new GeoLiteProvider("") // TODO get geolite key?
}
