package com.selfdualbrain.finatrapoc.core.hal

import com.selfdualbrain.finatrapoc.core.model.{LightState, SensorInfo, DeviceUniqueId, LightSourceInfo}

/**
  * Interface exposed by the bridge.
  * This interface is loosely inspired by (subset of) Philips Hue bridges API.
  */
trait BridgeHardwareApi {
  def getAllLights: Iterable[LightSourceInfo]
  def searchForNewLights(): Iterable[LightSourceInfo]
  def getLightAttributesAndState(id: DeviceUniqueId): Option[(LightSourceInfo, LightState)]
  def setLightState(id: DeviceUniqueId, state: LightState)
  def getAllSensors: Iterable[SensorInfo]
  def getSensorAttributes(id: DeviceUniqueId): Option[SensorInfo]
  def measureLightIntensity(id: DeviceUniqueId): Int
  def measureTemp(id: DeviceUniqueId): Double
  def reboot()

}
