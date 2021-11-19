package io.flowguard.uniplas.asnprovider

import akka.grpc.GrpcServiceException
import com.comcast.ip4s.{Cidr, IpAddress, IpLiteralSyntax}
import io.flowguard.uniplas.asnprovider.grpc.AsnNumRequest
import io.flowguard.uniplas.asnprovider.models.{AsnDatabase, AsnRecord}
import io.grpc.Status
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec

import scala.concurrent.ExecutionContext.Implicits.global

class AsnServiceImplTests extends AnyFlatSpec {
  val ipV4AsnRecords = Seq(
    AsnRecord(Cidr[IpAddress](ip"1.0.0.0", 24), "13335", "CLOUDFLARENET"),
    AsnRecord(Cidr[IpAddress](ip"1.0.4.0", 22), "38803", "Wirefreebroadband Pty Ltd")
  )
  val ipV6Records = Seq(
    AsnRecord(Cidr[IpAddress](ip"2001:200:1000::", 36), "2500", "WIDE Project")
  )

  implicit val asnLookup: AsnDatabase = AsnDatabase(ipV4AsnRecords, ipV6Records)
  val asnServiceImpl = new AsnServiceImpl()

  "Asn ipv4 lookup" should "find valid value" in {
    val request = AsnNumRequest("1.0.0.10")
    asnServiceImpl.getAsnNum(request).map { reply =>
      assert(reply.asnNum == "13335")
      assert(reply.asnName == "CLOUDFLARENET")
    }
  }

  it should "fail, because the value does not exist in the asn lookup db" in {
    val request = AsnNumRequest("2.2.2.2")
    ScalaFutures.whenReady(asnServiceImpl.getAsnNum(request).failed) { e =>
      val grpcErr = e.asInstanceOf[GrpcServiceException]
      assert(grpcErr.status.getCode == Status.NOT_FOUND.getCode)
    }
  }

  it should "fail, because of invalid format of ip address" in {
    val request = AsnNumRequest("2.invalid")
    ScalaFutures.whenReady(asnServiceImpl.getAsnNum(request).failed) { e =>
      val grpcErr = e.asInstanceOf[GrpcServiceException]
      assert(grpcErr.status.getCode == Status.INVALID_ARGUMENT.getCode)
    }
  }

  "Asn ipv6 lookup" should "find valid value" in {
    val request = AsnNumRequest("2001:0200:1fff:ffff:ffff:ffff:ffff:ffff")
    asnServiceImpl.getAsnNum(request).map { reply =>
      assert(reply.asnNum == "2500")
      assert(reply.asnName == "WIDE Project")
    }
  }

  it should "fail, because the value does not exist in the asn lookup db" in {
    val request = AsnNumRequest("db34:1564:9b37:41b4:4fd8:45cb:d613:04f8")
    ScalaFutures.whenReady(asnServiceImpl.getAsnNum(request).failed) { e =>
      val grpcErr = e.asInstanceOf[GrpcServiceException]
      assert(grpcErr.status.getCode == Status.NOT_FOUND.getCode)
    }
  }

  it should "fail, because of invalid format of ip address" in {
    val request = AsnNumRequest("db34:invalid")
    ScalaFutures.whenReady(asnServiceImpl.getAsnNum(request).failed) { e =>
      val grpcErr = e.asInstanceOf[GrpcServiceException]
      assert(grpcErr.status.getCode == Status.INVALID_ARGUMENT.getCode)
    }
  }
}
