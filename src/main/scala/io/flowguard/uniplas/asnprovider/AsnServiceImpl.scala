package io.flowguard.uniplas.asnprovider

import io.flowguard.uniplas.asnprovider.grpc._

import scala.concurrent.Future


class AsnServiceImpl extends AsnService {
  def getAsnNum(in: AsnNumRequest): Future[AsnNumReply] = {
    ???
  }
}
