package com.selfdualbrain.finatrapoc.core.model

class SensorInfo(
  val model: DeviceModelId,
  val serialNumber: DeviceUniqueId,
  val firmware: SoftwareVersion,
  val name: String,
  val quantity: MeasuredQuantity
) extends HardwareDeviceInfo {
}
