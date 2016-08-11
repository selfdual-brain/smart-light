package com.selfdualbrain.finatrapoc.core.model

abstract class HardwareDeviceInfo {
  def model: DeviceModelId
  def name: String
  def serialNumber: DeviceUniqueId
  def firmware: SoftwareVersion
}
