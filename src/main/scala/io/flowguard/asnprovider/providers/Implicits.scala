package io.flowguard.asnprovider.providers

import com.typesafe.config.ConfigFactory
import io.flowguard.asnprovider.models.AsnDatabase
import wvlet.log.LogSupport

object Implicits extends LogSupport {
  implicit val dummyAsnProvider: AsnProvider = new AsnProvider {
    override def load: AsnDatabase = AsnDatabase.empty()
  }

  /**
   * Load provider settings from application.conf
   */
  implicit val fromConfig: AsnProvider = {
    val config = ConfigFactory.load()
    config.getString("asn-service.db-provider.provider") match {
      case "dummy" =>
        warn("Using ASN dummy provider. This should be used only for testing!")
        dummyAsnProvider
      case "geolite" =>
        info("Using geolite ASN provider")
        val apiKey = {
          if (config.hasPath("asn-service.db-provider.api-key")) {
            info("Using geolite api key from application.conf")
            config.getString("asn-service.db-provider.api-key")
          } else {
            val geoLiteApiKeyEnv = "GEOLITE_API_KEY"
            info(s"Using geolite api key from $geoLiteApiKeyEnv env")
            System.getenv(geoLiteApiKeyEnv)
          }
        }
        new GeoLiteProvider(apiKey)
    }
  }
}
