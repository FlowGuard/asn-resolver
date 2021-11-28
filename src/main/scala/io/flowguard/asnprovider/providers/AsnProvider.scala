package io.flowguard.asnprovider.providers

import io.flowguard.asnprovider.models.AsnDatabase

trait AsnProvider {
  def load: AsnDatabase
}
