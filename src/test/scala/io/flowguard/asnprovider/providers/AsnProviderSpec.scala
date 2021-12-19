package io.flowguard.asnprovider.providers

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.comcast.ip4s.{Cidr, IpAddress, IpLiteralSyntax}
import io.flowguard.asnprovider.models.{AsnDatabase, AsnRecord}
import io.flowguard.asnprovider.providers.AsnProviderActor.{AsnNumRequest, AsnNumResponse}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

class AsnProviderSpec extends TestKit(ActorSystem("AsnProviderSpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val testRecord: AsnRecord = AsnRecord(Cidr[IpAddress](ip"1.0.0.0", 24), 13335, "CLOUDFLARENET")

  implicit val asnProvider: AsnProvider = new AsnProvider {
    override def load: AsnDatabase = AsnDatabase(
      Seq(testRecord) , // ipv4 records
      Seq.empty // ipv6 records
    )
  }

  "Asn provider" should {
    val asnProviderActor: ActorRef = system.actorOf(Props(new AsnProviderActor(None)), "asnProviderActor")
    "return some data on valid asn request" in {
      asnProviderActor ! AsnNumRequest(ip"1.0.0.0")
      expectMsg(AsnNumResponse(Some(testRecord)))
    }

    "return None because requested data doesn't exists" in {
      asnProviderActor ! AsnNumRequest(ip"1.1.1.1")
      expectMsg(AsnNumResponse(None))
    }
  }
}
