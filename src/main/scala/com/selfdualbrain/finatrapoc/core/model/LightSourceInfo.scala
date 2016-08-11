package com.selfdualbrain.finatrapoc.core.model

class LightSourceInfo(
  val model: DeviceModelId,
  val serialNumber: DeviceUniqueId,
  val firmware: SoftwareVersion,
  val name: String
)
  extends HardwareDeviceInfo
