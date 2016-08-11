package com.selfdualbrain.finatrapoc.core.hal

import com.selfdualbrain.finatrapoc.core.hal.BridgeHardwareSimulator.BridgeCommEmulator
import com.selfdualbrain.finatrapoc.core.model.{LightState, _}

import scala.collection.mutable
import scala.util.Random

/**
  * Simulates communiation to bridges.
  * Useful for demonstration and testing purposes.
  * Caution: the simulation is really simplistic, we do not simulate communication latency and errors.
  * Values measured by sensors are also simplistic - we simulate random changes within [0..100] range, starting from 50.
  */
class BridgeHardwareSimulator(
  bridges: Iterable[BridgeInfo],
  lights: Map[DeviceUniqueId, Iterable[LightSourceInfo]],
  sensors: Map[DeviceUniqueId, Iterable[SensorInfo]]
  ) extends BridgeDiscoveryApi
{

  private val bridgesInfoMap: Map[DeviceUniqueId, BridgeCommEmulator] = (bridges map (info => info.serialNumber -> createBridgeEmulatorFor(info))).toMap

  private def createBridgeEmulatorFor(info: BridgeInfo) = new BridgeCommEmulator(info, lights(info.serialNumber), sensors(info.serialNumber))

  override def discoverAllBridgesInScope: Iterable[BridgeInfo] = bridges

  override def getBridgeInterface(id: DeviceUniqueId): BridgeHardwareApi = bridgesInfoMap(id)
}

object BridgeHardwareSimulator {
  val random = new Random()

  class BridgeCommEmulator(bridgeInfo: BridgeInfo, lights: Iterable[LightSourceInfo], sensors: Iterable[SensorInfo]) extends BridgeHardwareApi {
    private val lightsMap = (lights map {each => each.serialNumber -> each}).toMap
    private val sensorsMap = (sensors map {each => each.serialNumber -> each}).toMap
    private val lightState: mutable.Map[DeviceUniqueId, LightState] = new mutable.HashMap[DeviceUniqueId, LightState]
    private val sensorState: mutable.Map[DeviceUniqueId, Double] = new mutable.HashMap[DeviceUniqueId, Double]

    //initialize states
    for (light <- lights)
      lightState += light.serialNumber -> new LightState(OnOffStatus.Off, brightness = 50, color = ColorTemp(5000))
    for (sensor <- sensors)
      sensorState += sensor.serialNumber -> 50.0

    override def getAllLights: Iterable[LightSourceInfo] = lights

    override def getAllSensors: Iterable[SensorInfo] = sensors

    override def getLightAttributesAndState(id: DeviceUniqueId): Option[(LightSourceInfo,LightState)] =
      lightsMap.get(id) map {light => (light, lightState(light.serialNumber))}

    override def setLightState(id: DeviceUniqueId, state: LightState): Unit = {
      lightsMap.get(id) match {
        case None => throw new Exception(s"light not found at this bridge, bridge=${bridgeInfo.serialNumber}, light=$id")
        case Some(light) => lightState(id) = state
      }
    }

    override def measureTemp(id: DeviceUniqueId): Double = simulateMeasurement(id)

    override def measureLightIntensity(id: DeviceUniqueId): Int = simulateMeasurement(id).toInt

    override def getSensorAttributes(id: DeviceUniqueId): Option[SensorInfo] = sensorsMap.get(id)

    override def searchForNewLights(): Iterable[LightSourceInfo] = Seq.empty

    override def reboot(): Unit = {
      //currently we do nothing
    }

    private def simulateMeasurement(id: DeviceUniqueId): Double = {
      sensorsMap.get(id) match {
        case None => throw new Exception(s"sensor not found at this bridge, bridge=${bridgeInfo.serialNumber}, sensor=$id")
        case Some(sensor) =>
          val oldMeasuredValue = sensorState(id)
          val newMeasurement = oldMeasuredValue + random.nextGaussian()
          val boundedMeasurement = math.max(0.0, math.min(newMeasurement, 100.0))
          sensorState(id) = boundedMeasurement
          boundedMeasurement
      }
    }
  }
}


