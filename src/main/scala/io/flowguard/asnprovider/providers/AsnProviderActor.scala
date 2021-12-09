package io.flowguard.asnprovider.providers

import akka.actor.{Actor, ActorLogging, Props, Timers}
import com.comcast.ip4s.IpAddress
import io.flowguard.asnprovider.models.{AsnDatabase, AsnRecord}
import Implicits.fromConfig

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * ASN provider actor
 * @param refreshRate ASN database refresh rate. Database is loaded only once at startup when it is None
 * @param asnProvider ASN provider
 */
class AsnProviderActor(refreshRate: Option[FiniteDuration])(implicit asnProvider: AsnProvider) extends Actor // TODO tests
  with ActorLogging // TODO logging library
  with Timers {
  import AsnProviderActor._

  refreshRate match {
    case None =>
      // load once
      log.debug("ASN database refresh rate => only at startup")
      timers.startSingleTimer(TimerKey, RefreshAsnDatabase, 1.seconds)
    case Some(interval) =>
      // load per interval
      log.debug(s"ASN database refresh rate => ${interval.toString()}")
      timers.startTimerWithFixedDelay(TimerKey, RefreshAsnDatabase, interval)
  }

  override def receive: Receive = search(AsnDatabase.empty())

  def search(asnDatabase: AsnDatabase): Receive = {

    case RefreshAsnDatabase =>
      log.info("ASN database refresh request")
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
