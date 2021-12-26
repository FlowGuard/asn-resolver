package io.flowguard.asnprovider.models

import com.comcast.ip4s.IpAddress
import com.comcast.ip4s.Cidr

/**
 * Asn lookup record
 * @param network Network in CIDR notation
 * @param autonomousSystemNumber ASN number
 * @param autonomousSystemOrganization ASN organization name
 */
final case class AsnRecord(network: Cidr[IpAddress], autonomousSystemNumber: Int, autonomousSystemOrganization: String)
