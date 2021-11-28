package io.flowguard.asnprovider.providers

import akka.actor.{Actor, ActorLogging, Props, Timers}
import com.comcast.ip4s.IpAddress
import io.flowguard.asnprovider.models.{AsnDatabase, AsnRecord}
import io.flowguard.asnprovider.models.AsnRecord
import Implicits.dummyAsnProvider

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class AsnProviderActor(implicit asnProvider: AsnProvider) extends Actor
  with ActorLogging
  with Timers {
  import AsnProviderActor._

  timers.startSingleTimer(TimerKey, RefreshAsnDatabase, 1.seconds) // TODO implement timers

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

  def param: Props = Props(new AsnProviderActor())
}
