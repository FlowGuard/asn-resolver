package io.flowguard.asnprovider

import akka.actor.{ActorRef, ActorSystem}
import akka.grpc.GrpcServiceException
import akka.grpc.scaladsl.{Metadata, MetadataBuilder}
import akka.pattern.ask
import akka.util.Timeout
import com.comcast.ip4s.IpAddress
import io.flowguard.asnprovider.grpc.{AsnNumReply, AsnNumRequest, AsnService}
import io.flowguard.asnprovider.providers.AsnProviderActor
import io.flowguard.asnprovider.grpc.AsnService
import io.grpc.Status
import wvlet.log.LogSupport

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class AsnServiceImpl(system: ActorSystem) extends AsnService with LogSupport {
  val asnProviderActor: ActorRef = system.actorOf(AsnProviderActor.param, "AsnProvider")

  val invalidIpAddressMetadata: Metadata = new MetadataBuilder().build()
  val asnNotFoundMetadata: Metadata = new MetadataBuilder().build()

  /**
   * Asn number lookup request
   * @param in Lookup request (ip address)
   * @return Lookup response (ASN number, ASN name)
   */
  def getAsnNum(in: AsnNumRequest): Future[AsnNumReply] = {
    import system.dispatcher
    implicit val timeout: Timeout = 2.seconds

    logger.debug(s"New ASN request => $in")

    IpAddress.fromString(in.ipAddress) match {
      case None =>
        logger.warn(s"Invalid ip address format for ${in.ipAddress}")
        Future.failed {
          new GrpcServiceException(Status.INVALID_ARGUMENT.withDescription("Invalid ip address"), invalidIpAddressMetadata)
        }
      case Some(ipAddress) =>
        (asnProviderActor ? AsnProviderActor.AsnNumRequest(ipAddress))
          .mapTo[AsnProviderActor.AsnNumResponse]
          .flatMap(r => r.asnRecord match {
            case Some(asnRecord) =>
              Future.successful(
                AsnNumReply(asnRecord.autonomousSystemNumber, asnRecord.autonomousSystemOrganization)
              )
            case None =>
              Future.failed(
                new GrpcServiceException(Status.NOT_FOUND.withDescription("ASN not found"), asnNotFoundMetadata)
              )
          })
    }
  }
}
