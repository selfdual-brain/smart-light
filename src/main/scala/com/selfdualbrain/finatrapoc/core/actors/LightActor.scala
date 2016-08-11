package com.selfdualbrain.finatrapoc.core.actors

import akka.actor.{ActorLogging, Actor, Props}
import com.selfdualbrain.finatrapoc.core.actors.LightActor.Res.BrightnessOutsideSupportedRange
import com.selfdualbrain.finatrapoc.core.actors.LightActor._
import com.selfdualbrain.finatrapoc.core.hal.BridgeHardwareApi
import com.selfdualbrain.finatrapoc.core.model._
import com.selfdualbrain.finatrapoc.utils.EventsBroadcaster

/**
  * Corresponds to one light source (=hardware device).
  */
class LightActor(val bridgeHardware: BridgeHardwareApi, val serialNumber: DeviceUniqueId) extends Actor with ActorLogging with EventsBroadcaster {
  var (info,state): (LightSourceInfo, LightState) = bridgeHardware.getLightAttributesAndState(serialNumber).get

  override def receive: Receive = super.receive orElse {
    case Req.GetDeviceInfo =>
      sender ! Res.DeviceInfo(info, state, isConnected = true)

    case Req.AdjustLight(brightness, color, effect) =>
      if (effect == "BLOW-THE-SYSTEM") {
        //this is kinda "easter egg" feature which allows us easy testing of finatra-akka-bridge resistance to stupid behaviour of akka actors
        log.debug("invoked BLOW-THE-SYSTEM hidden feature")
        sender ! Res.BlowTheSystem("bingo!")
      }
      else if (effect == "BE-DEAF") {
        //another "easter egg" to test situation when the reply message will never come (which will cause sender to blow with ask-pattern timeout)
        log.debug("invoked BE-DEAF hidden feature")
        //here we deliberatery do nothing
      }
      else if (effect != "NONE")
        sender ! Res.EffectNotSupported(effect)
      else if (brightness < 0 || brightness > 255)
        sender ! BrightnessOutsideSupportedRange(brightness, supportedRangeMin = 0, supportedRangeMax = 255)
      else {
        state = new LightState(state.onOffStatus, brightness, color)
        bridgeHardware.setLightState(info.serialNumber, state)
        trigger(LightSystemEvent.LightAdjusted(serialNumber, brightness, color))
        sender ! Res.DeviceInfo(info, state, isConnected = true)
      }

    case Req.SwitchOn =>
      state.onOffStatus match {
        case OnOffStatus.On => //do nothing, light is already ON
        case OnOffStatus.Off =>
          state = new LightState(OnOffStatus.On, state.brightness, state.color)
          bridgeHardware.setLightState(info.serialNumber, state)
          trigger(LightSystemEvent.LightSwitchedOn)
      }

    case Req.SwitchOff =>
      state.onOffStatus match {
        case OnOffStatus.Off => //do nothing, light is already OFF
        case OnOffStatus.On =>
          state = new LightState(OnOffStatus.Off, state.brightness, state.color)
          bridgeHardware.setLightState(info.serialNumber, state)
          trigger(LightSystemEvent.LightSwitchedOff)
      }
  }
}

object LightActor {
  def props(bridgeHardware: BridgeHardwareApi, serialNumber: DeviceUniqueId) = Props(new LightActor(bridgeHardware, serialNumber))

  object Req {
    case object GetDeviceInfo
    case class AdjustLight(brighness: Int, color: Color, effect: String)
    case object SwitchOn
    case object SwitchOff
  }

  object Res {
    case class DeviceInfo(info: LightSourceInfo, state: LightState, isConnected: Boolean)
    case class EffectNotSupported(name: String)
    case class BrightnessOutsideSupportedRange(value: Int, supportedRangeMin: Int, supportedRangeMax: Int)
    case class BlowTheSystem(comment: String)
  }

}
