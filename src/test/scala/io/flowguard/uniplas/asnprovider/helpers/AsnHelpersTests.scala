package io.flowguard.uniplas.asnprovider.helpers

import com.comcast.ip4s.{Cidr, IpAddress, IpLiteralSyntax}
import io.flowguard.uniplas.asnprovider.GeoLiteProvider
import io.flowguard.uniplas.asnprovider.models.AsnRecord
import org.scalatest.flatspec.AnyFlatSpec

class AsnHelpersTests extends AnyFlatSpec {
  "Geolite raw table" should "be converted to the seq of asn records" in {
    val testRawTable =
      """network,autonomous_system_number,autonomous_system_organization
        |1.0.0.0/24,13335,CLOUDFLARENET
        |1.0.4.0/22,38803,"Wirefreebroadband Pty Ltd"
        |1.0.64.0/18,18144,"Energia Communications,Inc."
        |""".stripMargin


    val result = GeoLiteProvider.geoLiteRawTableToAsnRecords(testRawTable)
    assert(result.size == 3)

    val nw1 = Cidr[IpAddress](ip"1.0.0.0", 24) // TODO not working
    val nw2 = Cidr[IpAddress](ip"1.0.4.0", 22)
    val nw3 = Cidr[IpAddress](ip"1.0.64.0", 18)

    result.foreach {
      case Right(AsnRecord(nw1, "13335", "CLOUDFLARENET")) =>
      case Right(AsnRecord(nw2, "38803", "Wirefreebroadband Pty Ltd")) =>
      case Right(AsnRecord(nw3, "18144", "Energia Communications,Inc.")) =>
      case _ => throw new AssertionError("invalid record")
    }
  }

  // TODO ipv6 test
}
