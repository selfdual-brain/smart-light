package com.selfdualbrain.finatrapoc.core.hal

import com.selfdualbrain.finatrapoc.core.model._

/**
  * Hardcoded collecion of lighting devices that we use as the basis for this proof-of-concept.
  */
object SampleHardwareDefinition {
  val bridges = Array(
    new BridgeInfo(
      model = DeviceModelId("AM-17"),
      serialNumber = DeviceUniqueId("B-001"),
      firmware = SoftwareVersion("1.4.2"),
      name = "Hue bridge XP",
      internalIpAddress = "192.168.1.171",
      macAddress = "00-14-22-01-23-45"
    ),
    new BridgeInfo(
      model = DeviceModelId("AX-10"),
      serialNumber = DeviceUniqueId("B-002"),
      firmware = SoftwareVersion("1.4.7"),
      name = "Hue bridge XP+",
      internalIpAddress = "192.168.1.300",
      macAddress = "00-14-22-E1-23-DD"
    )
  )

  val lights: Map[DeviceUniqueId, Iterable[LightSourceInfo]] = Map(
    DeviceUniqueId("B-001") -> Seq(
      new LightSourceInfo(model = DeviceModelId("LTW001"), serialNumber = DeviceUniqueId("L-03672178"), firmware = SoftwareVersion("1.0"), name = "Hue A19 White Ambience"),
      new LightSourceInfo(model = DeviceModelId("LWB006"), serialNumber = DeviceUniqueId("L-10003301"), firmware = SoftwareVersion("1.0"), name = "Hue A19 Lux"),
      new LightSourceInfo(model = DeviceModelId("LWB006"), serialNumber = DeviceUniqueId("L-10088055"), firmware = SoftwareVersion("1.1"), name = "Hue A19 White Ambience")
    ),
    DeviceUniqueId("B-002") -> Seq(
      new LightSourceInfo(model = DeviceModelId("LLC011"), serialNumber = DeviceUniqueId("L-01011127"), firmware = SoftwareVersion("0.8-beta"), name = "Hue Living Colors Bloom"),
      new LightSourceInfo(model = DeviceModelId("LCT003"), serialNumber = DeviceUniqueId("L-01011128"), firmware = SoftwareVersion("1.4"), name = "Hue Spot GU10")
    )
  )

  val sensors: Map[DeviceUniqueId, Iterable[SensorInfo]] = Map(
    DeviceUniqueId("B-001") -> Seq(
      new SensorInfo(model = DeviceModelId("CLIP-GENERIC"), DeviceUniqueId("S-40022"), firmware = SoftwareVersion("1.9"), name = "CLIP-temp", quantity = MeasuredQuantity.Temp),
      new SensorInfo(model = DeviceModelId("XCF-700"), DeviceUniqueId("S-10022"), firmware = SoftwareVersion("1.0"), name = "Daylight sensor", quantity = MeasuredQuantity.Daylight)
    ),
    DeviceUniqueId("B-002") -> Seq(
      new SensorInfo(model = DeviceModelId("XCF-700"), DeviceUniqueId("S-10029"), firmware = SoftwareVersion("1.0"), name = "Daylight sensor", quantity = MeasuredQuantity.Daylight)
    )
  )

  val halSimulator = new BridgeHardwareSimulator(bridges, lights, sensors)

}
