package io.flowguard.asnprovider.models

case class ConfigAsnService(dbProvider: ConfigDbProvider, bindAddress: String, bindPort: Int)
