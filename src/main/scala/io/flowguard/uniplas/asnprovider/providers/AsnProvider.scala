package io.flowguard.uniplas.asnprovider.providers

import io.flowguard.uniplas.asnprovider.models.AsnDatabase

trait AsnProvider {
  def load: AsnDatabase
}
