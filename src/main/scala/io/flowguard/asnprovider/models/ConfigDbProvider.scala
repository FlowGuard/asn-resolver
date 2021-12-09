package io.flowguard.asnprovider.models

import scala.concurrent.duration.FiniteDuration

case class ConfigDbProvider(provider: String, refreshRate: Option[FiniteDuration])
