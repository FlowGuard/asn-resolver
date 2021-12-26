package io.flowguard.asnprovider.models

final case class ConfigAsnService(dbProvider: ConfigDbProvider, bindAddress: String, bindPort: Int)
