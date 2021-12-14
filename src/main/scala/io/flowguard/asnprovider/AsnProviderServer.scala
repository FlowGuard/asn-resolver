package io.flowguard.asnprovider

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import io.flowguard.asnprovider.grpc.AsnServiceHandler
import wvlet.log.LogSupport

import scala.concurrent.{ExecutionContext, Future}

class AsnProviderServer(system: ActorSystem) extends LogSupport {
  def run(): Future[Http.ServerBinding] = {
    // akka boot code
    implicit val sys: ActorSystem = system
    implicit val ec: ExecutionContext = system.dispatcher

    // create service handlers
    val service: HttpRequest => Future[HttpResponse] = {
      AsnServiceHandler.withServerReflection(new AsnServiceImpl(system))
    }

    // bind service handlers
    val binding = Http()
      .newServerAt(Config.asnService.bindAddress, Config.asnService.bindPort)
      .bind(service)
    binding.foreach(b => logger.info(s"gRPC server bound to ${b.localAddress}"))

    binding
  }
}

object AsnProviderServer {
  def apply(system: ActorSystem) = new AsnProviderServer(system)
}
