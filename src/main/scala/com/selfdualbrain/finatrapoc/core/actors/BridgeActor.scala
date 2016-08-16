package com.selfdualbrain.finatrapoc.core.actors

import akka.actor.{ActorLogging, Actor, ActorRef, Props}
import com.selfdualbrain.finatrapoc.core.actors.BridgeActor._
import com.selfdualbrain.finatrapoc.core.config.Config
import com.selfdualbrain.finatrapoc.core.hal.BridgeHardwareApi
import com.selfdualbrain.finatrapoc.core.model._
import com.selfdualbrain.finatrapoc.utils.EventsBroadcaster

import scala.collection.mutable

/**
  * Corresponds to one "bridge" (hardware device).
  */
class BridgeActor(val bridgeHardware: BridgeHardwareApi, val info: BridgeInfo) extends Actor with ActorLogging with EventsBroadcaster {
  val lights: mutable.Map[DeviceUniqueId, ActorRef] = new mutable.HashMap[DeviceUniqueId, ActorRef]
  val sensors: mutable.Map[DeviceUniqueId, ActorRef] = new mutable.HashMap[DeviceUniqueId, ActorRef]

  //initalization of child actors
  for (device <- bridgeHardware.getAllLights)
    this.createLightActor(device.serialNumber)
  for (device <- bridgeHardware.getAllSensors)
    this.createSensorActor(device.serialNumber)

  log.info(s"actor for bridge ${info.serialNumber.value} is ready, actor-path=${self.path}")

  override def receive: Receive = super.receive orElse {
    case Req.GetDeviceInfo =>
      sender ! Res.DeviceInfo(info, lights.toMap, sensors.toMap) //sending immutable copies of lights and sensor maps

    case Req.ForceDevicesDiscovery =>
      for (newLightDevice <- bridgeHardware.searchForNewLights())
        this.createLightActor(newLightDevice.serialNumber)

    case Req.Restart =>
      bridgeHardware.reboot()
      trigger(LightSystemEvent.BridgeReboot)

  }

  def createLightActor(serialNumber: DeviceUniqueId) = {
    val newChildActor = context.actorOf(LightActor.props(bridgeHardware, serialNumber), "light-" + serialNumber.value)
    lights += serialNumber -> newChildActor
    this.listenAndReBroadcastEventsFrom(newChildActor)
    trigger(LightSystemEvent.NewLightDiscovered(serialNumber))
  }

  def createSensorActor(serialNumber: DeviceUniqueId) = {
    val newChildActor = context.actorOf(SensorActor.props(bridgeHardware, serialNumber, Config.sensorsPollingInterval, Config.sensorsPrecision), "sensor-" + serialNumber.value)
    sensors += serialNumber -> newChildActor
    this.listenAndReBroadcastEventsFrom(newChildActor)
    trigger(LightSystemEvent.NewSensorDiscovered(serialNumber))
  }

}

object BridgeActor {
  def props(bridgeHardware: BridgeHardwareApi, info: BridgeInfo) = Props(new BridgeActor(bridgeHardware, info))

  object Req {
    case object GetDeviceInfo
    case object ForceDevicesDiscovery
    case object Restart
  }

  object Res {
    case class DeviceInfo(info: BridgeInfo, lights: Map[DeviceUniqueId, ActorRef], sensors: Map[DeviceUniqueId, ActorRef])
    case object Test
  }

}
