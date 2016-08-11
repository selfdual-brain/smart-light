package com.selfdualbrain.finatrapoc.core.actors

import akka.actor.{ActorLogging, Props, Actor}
import com.selfdualbrain.finatrapoc.core.actors.SensorActor.Req.GetDeviceInfo
import com.selfdualbrain.finatrapoc.core.hal.BridgeHardwareApi
import com.selfdualbrain.finatrapoc.core.model._
import SensorActor._
import com.selfdualbrain.finatrapoc.utils.EventsBroadcaster

import scala.concurrent.duration.FiniteDuration

/**
  * Corresponds to one sensor (=hardware device).
  */
class SensorActor(val bridgeHardware: BridgeHardwareApi, val serialNumber: DeviceUniqueId, pollingDelay: FiniteDuration, precision: Double) extends Actor with ActorLogging with EventsBroadcaster {
  import context.dispatcher

  var info: SensorInfo = bridgeHardware.getSensorAttributes(serialNumber).get
  var lastValueMeasured: Double = 0
  var lastValuePublished: Double = 0

  initializeMeasurementPolling()

  override def receive: Receive = super.receive orElse {
    case GetDeviceInfo =>
      sender ! Res.DeviceInfo(info, lastValueMeasured)

    case Internal.Tick =>
      lastValueMeasured = peekMeasuredValueFromTheHardware()
      if (math.abs(lastValueMeasured - lastValuePublished) > precision) {
        trigger(LightSystemEvent.MeasuredNewValue(lastValueMeasured))
        lastValuePublished = lastValueMeasured
      }
  }

  def initializeMeasurementPolling(): Unit = {
    context.system.scheduler.schedule(pollingDelay, pollingDelay, self, Internal.Tick)
  }

  def peekMeasuredValueFromTheHardware(): Double = info.quantity match {
      case MeasuredQuantity.Daylight => bridgeHardware.measureLightIntensity(serialNumber)
      case MeasuredQuantity.Temp => bridgeHardware.measureTemp(serialNumber)
      case MeasuredQuantity.Humidity =>
        throw new Exception("humidity sensors currently not supported")
    }
}

object SensorActor {
  def props(bridgeHardware: BridgeHardwareApi, serialNumber: DeviceUniqueId, pollingDelay: FiniteDuration, precision: Double) =
    Props(new SensorActor(bridgeHardware, serialNumber, pollingDelay, precision))

  object Internal {
    case object Tick
  }

  object Req {
    case object GetDeviceInfo
  }

  object Res {
    case class DeviceInfo(info: com.selfdualbrain.finatrapoc.core.model.SensorInfo, measuredValue: Double)
  }

}