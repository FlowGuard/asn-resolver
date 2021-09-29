package io.flowguard.uniplas.asnprovider

import collection.JavaConverters.*
import io.flowguard.uniplas.asnprovider.helpers.getValidAndReportFailed
import io.flowguard.uniplas.asnprovider.models.AsnRecord

import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.zip
import java.util.zip.ZipInputStream
import scala.annotation.tailrec
import scala.io.Source
import io.flowguard.uniplas.asnprovider.helpers.DecodedRecord
import com.comcast.ip4s.Cidr

trait AsnProvider {
    def load: Seq[AsnRecord]
}

class GeoLiteProvider(apiKey: String) extends AsnProvider {
  val permaLink =
    s"https://download.maxmind.com/app/geoip_download?edition_id=GeoLite2-ASN-CSV&license_key=$apiKey&suffix=zip"

  def load: Seq[AsnRecord] = {
    def getAsnRecordsFromRaw(rawAsnTable: String) = 
      GeoLiteProvider.geoLiteRawTableToAsnRecords(rawAsnTable).flatMap(_.getValidAndReportFailed)

    // download from source
    val asnBlocks = downloadFromMaxmindAndExtract(permaLink) // TODO download should be functional, method must be testable 
    
    // convert to proper format
    asnBlocks match {
      case (Some(ipv4RawAsnTable), Some(ipv6RawAsnTable)) =>
        getAsnRecordsFromRaw(ipv4RawAsnTable) ++ getAsnRecordsFromRaw(ipv6RawAsnTable) 
      case (Some(ipv4RawAsnTable), None) =>
        getAsnRecordsFromRaw(ipv4RawAsnTable)
        // TODO log error
      case (None, Some(ipv6RawAsnTable)) =>
        getAsnRecordsFromRaw(ipv6RawAsnTable)
        // TODO log error
      case (None, None) => 
        // TODO log error
        Seq.empty[AsnRecord]
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
