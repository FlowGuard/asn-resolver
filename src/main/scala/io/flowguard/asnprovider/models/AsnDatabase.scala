package io.flowguard.asnprovider.models

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

  def searchByAutonomousNumber(autonomousNumber: Int): Seq[AsnRecord] =
    concatRecords.filter(_.autonomousSystemNumber == autonomousNumber)
}

object AsnDatabase {
  def apply(ipv4AsnRecords: Seq[AsnRecord], ipv6AsnRecords: Seq[AsnRecord]) = new AsnDatabase(ipv4AsnRecords, ipv6AsnRecords)
  def empty() = new AsnDatabase(Seq.empty[AsnRecord], Seq.empty[AsnRecord])
}
