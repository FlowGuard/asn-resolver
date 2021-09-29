package io.flowguard.uniplas.asnprovider.models

import com.comcast.ip4s.IpAddress
import com.comcast.ip4s.Cidr

case class AsnRecord(network: Cidr[IpAddress], autonomousSystemNumber: String, autonomousSystemOrganization: String)
