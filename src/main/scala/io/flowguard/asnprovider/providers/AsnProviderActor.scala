package io.flowguard.asnprovider.providers

import akka.actor.{Actor, ActorLogging, Props, Timers}
import com.comcast.ip4s.IpAddress
import io.flowguard.asnprovider.models.{AsnDatabase, AsnRecord}
import Implicits.fromConfig
import wvlet.log.LogSupport

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * ASN provider actor
 * @param refreshRate ASN database refresh rate. Database is loaded only once at startup when it is None
 * @param asnProvider ASN provider
 */
class AsnProviderActor(refreshRate: Option[FiniteDuration])(implicit asnProvider: AsnProvider) extends Actor // TODO tests
  with LogSupport
  with Timers {
  import AsnProviderActor._

  refreshRate match {
    case None =>
    case Some(interval) =>
      // load per interval
      logger.info(s"ASN database refresh rate => ${interval.toString()}")
      timers.startTimerWithFixedDelay(TimerKey, RefreshAsnDatabase, interval)
  }

  override def receive: Receive = {
    logger.info("ASN database -> loading at startup")
    self ! RefreshAsnDatabase
    search(AsnDatabase.empty())
  }

  def search(asnDatabase: AsnDatabase): Receive = {

    case RefreshAsnDatabase =>
      logger.info("ASN database refresh request")
      context.become(search(asnProvider.load))

    case AsnNumRequest(ipAddress) =>
      val asnRecord = asnDatabase.searchByIpAddress(ipAddress)
      sender() ! AsnNumResponse(asnRecord)
  }
}

object AsnProviderActor {
  case object TimerKey
  case object RefreshAsnDatabase
  case class AsnNumRequest(ipAddress: IpAddress)
  case class AsnNumResponse(asnRecord: Option[AsnRecord])

  def param(refreshRate: Option[FiniteDuration] = None): Props = Props(new AsnProviderActor(refreshRate))
}
