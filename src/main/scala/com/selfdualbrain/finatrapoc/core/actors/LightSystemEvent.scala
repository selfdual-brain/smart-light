package com.selfdualbrain.finatrapoc.core.actors

import com.selfdualbrain.finatrapoc.core.model.{Color, DeviceUniqueId}

/**
  * Definition of events broadcast by light system actors.
  */
object LightSystemEvent {

  //light system
  case class NewBridgeConnected(deviceId: DeviceUniqueId)

  //bridge
  case class NewLightDiscovered(deviceId: DeviceUniqueId)
  case class NewSensorDiscovered(deviceId: DeviceUniqueId)
  case class BridgeConnectionEstablished()
  case class BridgeConnectionLost()
  case object BridgeReboot

  //light
  case class LightSwitchedOn(deviceId: DeviceUniqueId)
  case class LightSwitchedOff(deviceId: DeviceUniqueId)
  case class LightAdjusted(deviceId: DeviceUniqueId, brighness: Int, color: Color)
  case class LightConnectionEstablished(deviceId: DeviceUniqueId)
  case class LightConnectionLost(deviceId: DeviceUniqueId)

  //sensor
  case class MeasuredNewValue(value: Double)
  case class SensorConnectionEstablished(deviceId: DeviceUniqueId)
  case class SensorConnectionLost(deviceId: DeviceUniqueId)
}
