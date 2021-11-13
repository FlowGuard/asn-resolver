package io.flowguard.uniplas.asnprovider

import com.comcast.ip4s.{Cidr, IpAddress}
import io.flowguard.uniplas.asnprovider.helpers.{DecodedRecord, getValidAndReportFailed}
import io.flowguard.uniplas.asnprovider.models.{AsnDatabase, AsnRecord}
import wvlet.log.LogSupport

import java.io.{ByteArrayInputStream, FileInputStream, FileOutputStream}
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.zip.ZipInputStream
import scala.annotation.tailrec
import scala.collection.JavaConverters.*
import scala.io.Source

 // TODO to separate model



trait AsnProvider {
  def load: AsnDatabase
}

class GeoLiteProvider(apiKey: String) extends AsnProvider with LogSupport {
  val permaLink =
    s"https://download.maxmind.com/app/geoip_download?edition_id=GeoLite2-ASN-CSV&license_key=$apiKey&suffix=zip"

  def load: AsnDatabase = {
    def getAsnRecordsFromRaw(rawAsnTable: String) = 
      GeoLiteProvider.geoLiteRawTableToAsnRecords(rawAsnTable).flatMap(_.getValidAndReportFailed)

    info("Loading Maxmind ASN database...")
    // download from source
    val asnBlocks = downloadFromMaxmindAndExtract(permaLink) // TODO download should be functional, method must be testable 
    
    // convert to proper format
    asnBlocks match {
      case (Some(ipv4RawAsnTable), Some(ipv6RawAsnTable)) =>
        AsnDatabase(getAsnRecordsFromRaw(ipv4RawAsnTable), getAsnRecordsFromRaw(ipv6RawAsnTable))
      case (Some(ipv4RawAsnTable), None) =>
        error("Can't parse ipv6 maxmind table")
        AsnDatabase(getAsnRecordsFromRaw(ipv4RawAsnTable), Seq.empty)
      case (None, Some(ipv6RawAsnTable)) =>
        error("Can't parse ipv4 maxmind table")
        getAsnRecordsFromRaw(ipv6RawAsnTable)
        AsnDatabase(Seq.empty, getAsnRecordsFromRaw(ipv6RawAsnTable))
      case (None, None) => 
        error("Can't parse both ipv4 and ipv6 maxmind table")
        AsnDatabase(Seq.empty, Seq.empty)
    }
  }

  // return (ipv4table, ipv6table)
  def downloadFromMaxmindAndExtract(permaLink: String): (Option[String], Option[String]) = {
    def readAsnFile(zs: ZipInputStream): String = {
      val sb = new StringBuilder
      val buffer =  new Array[Byte](1024)
      LazyList.continually(zs.read(buffer, 0, 1024)).takeWhile(_ != -1).foreach(l => sb.append(new String(buffer, 0, l)))
      sb.toString()
    }

    val geoLiteIpv4File = "GeoLite2-ASN-Blocks-IPv4.csv"
    val geoLiteIpv6File = "GeoLite2-ASN-Blocks-IPv6.csv"

    val urlStream = new URL(permaLink).openStream()
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
  /** Convert raw Maxmind ASN table to the decoded record **/
  def geoLiteRawTableToAsnRecords(rawTable: String): Seq[DecodedRecord] = {
    val csvRecords = rawTable.split('\n').toSeq
    csvRecords.tail.map { // tail - omit csv head
      case s"$network,$autSysNumber,$autSysOrg" =>
        Cidr.fromString(network) match {
          case Some(cidr) =>  
            Right(AsnRecord(
              cidr,
              autSysNumber,
              autSysOrg.replaceAll("\"", ""))) // remove quotations mark from composed strings
          case None =>
            Left(s"Invalid Cidr network $network") 
        }
      case line => Left(line)
    }
  }
}
