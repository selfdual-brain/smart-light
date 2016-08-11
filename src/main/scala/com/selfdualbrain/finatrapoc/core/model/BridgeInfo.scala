package com.selfdualbrain.finatrapoc.core.model

class BridgeInfo(
  val model: DeviceModelId,
  val serialNumber: DeviceUniqueId,
  val firmware: SoftwareVersion,
  val name: String,
  val internalIpAddress: String,
  val macAddress: String
) extends HardwareDeviceInfo {

}
