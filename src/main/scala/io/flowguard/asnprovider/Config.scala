package io.flowguard.asnprovider

import com.typesafe.config.ConfigFactory
import io.flowguard.asnprovider.models.{ConfigAsnService, ConfigDbProvider}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

object Config {
  /**
   * Asn service configuration
   */
  val asnService: ConfigAsnService = {
    val config = ConfigFactory.load().getConfig("asn-service")
    val bindAddress = config.getString("bind-address")
    val bindPort = config.getString("bind-port")

    val asnProvider = config.getString("db-provider.provider")
    val refreshRate = {
      if (config.hasPath("db-provider.refresh-rate")) {
        val refreshRate = config.getDuration("db-provider.refresh-rate")
        Some(FiniteDuration(refreshRate.toNanos, TimeUnit.NANOSECONDS))
      }
      else
        None
    }

    val dbProvider = ConfigDbProvider(asnProvider, refreshRate)
    ConfigAsnService(dbProvider, bindAddress, bindPort)
  }
}
