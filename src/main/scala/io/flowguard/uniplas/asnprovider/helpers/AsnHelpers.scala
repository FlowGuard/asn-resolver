package io.flowguard.uniplas.asnprovider.helpers

import com.comcast.ip4s.Cidr
import com.comcast.ip4s.IpAddress
import io.flowguard.uniplas.asnprovider.models.AsnRecord

type DecodedRecord = Either[String, AsnRecord]

extension(d: DecodedRecord)
  def getValidAndReportFailed: Option[AsnRecord] = {
    d match {
      case Right(asnRecord) => Some(asnRecord)
      case Left(errorLine) =>
        println(s"can't decode $errorLine") // TODO better logging
        None
    }
  }
