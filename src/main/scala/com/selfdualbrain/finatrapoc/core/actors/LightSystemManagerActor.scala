package com.selfdualbrain.finatrapoc.core.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.selfdualbrain.finatrapoc.core.actors.LightSystemManagementActor._
import com.selfdualbrain.finatrapoc.core.hal.{BridgeDiscoveryApi, BridgeHardwareApi}
import com.selfdualbrain.finatrapoc.core.model.{BridgeInfo, DeviceUniqueId}
import com.selfdualbrain.finatrapoc.utils.EventsBroadcaster

import scala.collection.mutable

/**
  * Master "manager" of intelligent lighing system.
  * All bridges are childen of this actor.
  */
class LightSystemManagerActor(bridgeDiscovery: BridgeDiscoveryApi) extends Actor with ActorLogging with EventsBroadcaster {
  val bridges: mutable.Map[DeviceUniqueId, ActorRef] = new mutable.HashMap[DeviceUniqueId, ActorRef]

  for (deviceInfo <- bridgeDiscovery.discoverAllBridgesInScope)
    createBridgeActor(deviceInfo, bridgeDiscovery.getBridgeInterface(deviceInfo.serialNumber))

  override def receive: Receive = super.receive orElse {
    case Req.ListConnectedBridges =>
      log.debug("light system manager: got ListConnectedBridges request")
      sender ! Res.ConnectedBridges(bridges.toMap)
  }

  override protected def handleEvent(origin: ActorRef, payload: Any): Unit = {
    log.debug(s"EVENT triggered by ${origin.path.name}: $payload")
  }

  def createBridgeActor(deviceInfo: BridgeInfo, hardwareApi: BridgeHardwareApi): Unit = {
    val newChildActor = context.actorOf(BridgeActor.props(hardwareApi, deviceInfo), "bridge-" + deviceInfo.serialNumber.value)
    bridges += deviceInfo.serialNumber -> newChildActor
    this.listenAndReBroadcastEventsFrom(newChildActor)
    trigger(LightSystemEvent.NewBridgeConnected(deviceInfo.serialNumber))
  }
}

object LightSystemManagementActor {
  def props(bridgeDiscovery: BridgeDiscoveryApi) = Props(new LightSystemManagerActor(bridgeDiscovery))

  object Req {
    case object ListConnectedBridges
  }

  object Res {
    case class ConnectedBridges(map: Map[DeviceUniqueId, ActorRef])
  }

}
