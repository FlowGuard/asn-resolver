package io.flowguard.asnprovider.providers

import com.comcast.ip4s.Cidr
import io.flowguard.asnprovider.models.{AsnDatabase, AsnRecord}
import io.flowguard.asnprovider.models.AsnRecord
import GeoLiteProvider.DecodedRecord
import wvlet.log.LogSupport

import java.net.URL
import java.util.zip.ZipInputStream
import scala.util.Try

class GeoLiteProvider(apiKey: String) extends AsnProvider with LogSupport {
  val staticLink =
    s"https://download.maxmind.com/app/geoip_download?edition_id=GeoLite2-ASN-CSV&license_key=$apiKey&suffix=zip"

  def getValidOrReportFailed(d: DecodedRecord): Option[AsnRecord] = {
    d match {
      case Right(asnRecord) => Some(asnRecord)
      case Left(errorLine) =>
        logger.warn(s"can't decode $errorLine")
        println(s"can't decode $errorLine")
        None
    }
  }

  def load: AsnDatabase = {
    def getAsnRecordsFromRaw(rawAsnTable: String): Seq[AsnRecord] =
      GeoLiteProvider.geoLiteRawTableToAsnRecords(rawAsnTable).flatMap(getValidOrReportFailed)

    info("Loading Maxmind ASN database...")
    // download from source
    val asnBlocks = downloadFromMaxmindAndExtract(staticLink) // TODO download should be functional, method must be testable

    // convert to proper format
    asnBlocks match {
      case (Some(ipv4RawAsnTable), Some(ipv6RawAsnTable)) =>
        val ipv4AsnRecords = getAsnRecordsFromRaw(ipv4RawAsnTable)
        val ipv6AsnRecords = getAsnRecordsFromRaw(ipv6RawAsnTable)
        AsnDatabase(ipv4AsnRecords, ipv6AsnRecords)
      case (Some(ipv4RawAsnTable), None) =>
        error("Can't parse ipv6 maxmind table")
        val ipv4AsnRecords = getAsnRecordsFromRaw(ipv4RawAsnTable)
        AsnDatabase(ipv4AsnRecords, Seq.empty)
      case (None, Some(ipv6RawAsnTable)) =>
        error("Can't parse ipv4 maxmind table")
        val ipv6AsnRecords = getAsnRecordsFromRaw(ipv6RawAsnTable)
        AsnDatabase(Seq.empty, ipv6AsnRecords)
      case (None, None) =>
        error("Can't parse both ipv4 and ipv6 maxmind table")
        AsnDatabase(Seq.empty, Seq.empty)
    }
  }

  // return (ipv4table, ipv6table)
  def downloadFromMaxmindAndExtract(maxmindLink: String): (Option[String], Option[String]) = {
    def readAsnFile(zs: ZipInputStream): String = {
      val sb = new StringBuilder
      val buffer =  new Array[Byte](1024)
      LazyList.continually(zs.read(buffer, 0, 1024)).takeWhile(_ != -1).foreach(l => sb.append(new String(buffer, 0, l)))
      sb.toString()
    }

    val geoLiteIpv4File = "GeoLite2-ASN-Blocks-IPv4.csv"
    val geoLiteIpv6File = "GeoLite2-ASN-Blocks-IPv6.csv"

    val urlStream = new URL(maxmindLink).openStream()
    val zipStream = new ZipInputStream(urlStream)

    val asnBlocks = LazyList.continually(zipStream.getNextEntry).takeWhile(_ != null).flatMap { ze =>
      if (ze.getName.contains(geoLiteIpv4File))
        Some(("ipv4", readAsnFile(zipStream)))
      else if (ze.getName.contains(geoLiteIpv6File))
        Some("ipv6", readAsnFile(zipStream))
      else
        None
    }.toMap

    // close all streams
    zipStream.close()
    urlStream.close()
    (asnBlocks.get("ipv4"), asnBlocks.get("ipv6"))
  }
}

object GeoLiteProvider {
  type DecodedRecord = Either[String, AsnRecord]

  /** Convert raw Maxmind ASN table to the decoded record **/
  def geoLiteRawTableToAsnRecords(rawTable: String): Seq[DecodedRecord] = {
    val csvRecords = rawTable.split('\n').toSeq
    csvRecords.tail.map { // tail - omit csv head
      case s"$network,$autSysNumber,$autSysOrg" =>
        Try(autSysNumber.toInt).toOption match {
          case None =>
            Left(s"Invalid Autonomous System Number $autSysNumber")
          case Some(validAsn) =>
            Cidr.fromString(network) match {
              case Some(cidr) =>
                Right(AsnRecord(
                  cidr,
                  validAsn,
                  autSysOrg.replaceAll("\"", ""))) // remove quotations mark from composed strings
              case None =>
                Left(s"Invalid Cidr network $network")
            }
        }
      case line => Left(line)
    }
  }
}
