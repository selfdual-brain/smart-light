package com.selfdualbrain.finatrapoc.webservice

import com.selfdualbrain.finatrapoc.core.model.DeviceUniqueId

/**
  * Defines actors/resources namespace structure.
  */
trait PathsMapper {
  def rootManager: String
  def bridge(serialNumber: DeviceUniqueId): String
  def light(bridge: DeviceUniqueId, light: DeviceUniqueId): String
  def sensor(bridge: DeviceUniqueId, sensor: DeviceUniqueId): String
}

object ActorPaths extends PathsMapper {
  override def rootManager: String = "akka://galaxy/user/root-manager"
  override def bridge(serialNumber: DeviceUniqueId): String = s"akka://galaxy/user/root-manager/bridge-${serialNumber.value}"
  override def light(bridge: DeviceUniqueId, light: DeviceUniqueId): String = s"akka://galaxy/user/root-manager/bridge-${bridge.value}/light-${light.value}"
  override def sensor(bridge: DeviceUniqueId, sensor: DeviceUniqueId): String = s"akka://galaxy/user/root-manager/bridge-${bridge.value}/sensor-${sensor.value}"
}

object WsResourcePaths extends PathsMapper {
  override def rootManager: String = "/system"
  override def bridge(serialNumber: DeviceUniqueId): String = s"/bridge/${serialNumber.value}"
  override def light(bridge: DeviceUniqueId, light: DeviceUniqueId): String = s"bridge/${bridge.value}/light/${light.value}"
  override def sensor(bridge: DeviceUniqueId, sensor: DeviceUniqueId): String = s"bridge/${bridge.value}/sensor/${sensor.value}"
}
