package io.flowguard.uniplas.asnprovider

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import io.flowguard.uniplas.asnprovider.grpc.AsnServiceHandler
import io.flowguard.uniplas.asnprovider.models.AsnDatabase
import wvlet.log.LogSupport

import scala.concurrent.{ExecutionContext, Future}

class AsnProviderServer(system: ActorSystem) extends LogSupport {
  def run(): Future[Http.ServerBinding] = {
    // akka boot code
    implicit val sys: ActorSystem = system
    implicit val ec: ExecutionContext = sys.dispatcher
    implicit lazy val asnDatabse: AsnDatabase = AsnDatabase.empty() // TODO

    // create service handlers
    val service: HttpRequest => Future[HttpResponse] = {
      AsnServiceHandler.withServerReflection((new AsnServiceImpl()))
    }

    // bind service handlers
    val binding = Http()
      .newServerAt("127.0.0.1", 8090)
      .bind(service) // TODO to config
    binding.foreach(b => logger.info(s"gRPC server bound to ${b.localAddress}"))

    binding
  }
}

object AsnProviderServer {
  def apply(system: ActorSystem) = new AsnProviderServer(system)
}
