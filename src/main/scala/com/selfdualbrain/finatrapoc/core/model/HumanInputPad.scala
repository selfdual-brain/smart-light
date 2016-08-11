package com.selfdualbrain.finatrapoc.core.model

class HumanInputPad(
  val model: DeviceModelId,
  val serialNumber: DeviceUniqueId,
  val firmware: SoftwareVersion,
  val connectionStatus: ConnectionStatus,
  val name: String,
  val buttons: Array[String]
) extends HardwareDeviceInfo

