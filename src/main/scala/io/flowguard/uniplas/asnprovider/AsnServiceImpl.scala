package io.flowguard.uniplas.asnprovider

import akka.grpc.GrpcServiceException
import akka.grpc.scaladsl.{Metadata, MetadataBuilder}
import com.comcast.ip4s.IpAddress
import io.flowguard.uniplas.asnprovider.grpc.{AsnNumReply, AsnNumRequest, AsnService}
import io.flowguard.uniplas.asnprovider.models.AsnDatabase
import io.grpc.Status
import wvlet.log.LogSupport

import scala.concurrent.Future

/**
 * Asn lookup service
 * @param asnLookupDatabase Asn lookup db
 */
class AsnServiceImpl(implicit asnLookupDatabase: AsnDatabase) extends AsnService with LogSupport {

  val invalidIpAddressMetadata: Metadata = new MetadataBuilder().build()
  val asnNotFoundMetadata: Metadata = new MetadataBuilder().build()

  /**
   * Asn number lookup request
   * @param in Lookup request (ip address)
   * @return Lookup response (ASN number, ASN name)
   */
  def getAsnNum(in: AsnNumRequest): Future[AsnNumReply] = {
    logger.debug(s"New ASN request => $in")

    IpAddress.fromString(in.ipAddress) match {
      case None =>
        logger.warn(s"Invalid ip address format for ${in.ipAddress}")
        Future.failed {
          new GrpcServiceException(Status.INVALID_ARGUMENT.withDescription("Invalid ip address"), invalidIpAddressMetadata)
        }
      case Some(ipAddress) =>
        asnLookupDatabase.searchByIpAddress(ipAddress) match {
          case None =>
            logger.warn(s"ASN not found for ip address $ipAddress")
            Future.failed {
              new GrpcServiceException(Status.NOT_FOUND.withDescription("ASN not found"), asnNotFoundMetadata)
            }
          case Some(asnRecord) =>
            Future.successful {
              AsnNumReply(asnRecord.autonomousSystemNumber, asnRecord.autonomousSystemOrganization)
            }
        }
    }
  }
}
