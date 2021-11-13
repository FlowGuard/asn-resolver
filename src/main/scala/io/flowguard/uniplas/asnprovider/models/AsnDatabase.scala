package io.flowguard.uniplas.asnprovider.models

import com.comcast.ip4s.IpAddress

/**
 * ASN lookups database
 * @param ipv4AsnRecords IPv4 asn records
 * @param ipv6AsnRecords IPv6 asn records
 */
class AsnDatabase(val ipv4AsnRecords: Seq[AsnRecord], val ipv6AsnRecords: Seq[AsnRecord]) {
  private val concatRecords = ipv4AsnRecords ++ ipv6AsnRecords

  def searchByIpAddress(ipAddress: IpAddress): Option[AsnRecord] =
    concatRecords.find(_.network.contains(ipAddress))

  def searchByAutonomousNumber(autonomousNumber: String): Seq[AsnRecord] = // TODO autonomous system number should be int
    concatRecords.filter(_.autonomousSystemNumber == autonomousNumber)
}

object AsnDatabase {
  def empty() = new AsnDatabase(Seq.empty[AsnRecord], Seq.empty[AsnRecord])
}