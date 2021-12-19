package io.flowguard.asnprovider.providers

import org.scalatest.flatspec.AnyFlatSpec

class GeoliteProviderTests extends AnyFlatSpec {
  "Geolite raw table" should "be converted to the seq of asn records" in {
    val testRawTable =
      """network,autonomous_system_number,autonomous_system_organization
        |1.0.0.0/24,13335,CLOUDFLARENET
        |1.0.4.0/22,38803,"Wirefreebroadband Pty Ltd"
        |1.0.64.0/18,18144,"Energia Communications,Inc."
        |2001:200:a00::/39,2500,"WIDE Project"
        |2001:200:900::/40,7660,"Asia Pacific Advanced Network - Japan"
        |""".stripMargin


    val result = GeoLiteProvider.geoLiteRawTableToAsnRecords(testRawTable)
    assert(result.size == 5)
  }
}
