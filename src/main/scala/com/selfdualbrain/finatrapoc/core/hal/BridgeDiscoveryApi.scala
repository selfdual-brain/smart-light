package com.selfdualbrain.finatrapoc.core.hal

import com.selfdualbrain.finatrapoc.core.model.{DeviceUniqueId, BridgeInfo}

/**
  * Contract of bridge discovery layer.
  */
trait BridgeDiscoveryApi {
  def discoverAllBridgesInScope: Iterable[BridgeInfo]
  def getBridgeInterface(id: DeviceUniqueId): BridgeHardwareApi
}
