package com.selfdualbrain.finatrapoc.webservice

import com.selfdualbrain.finatrapoc.core.model.DeviceUniqueId
import com.twitter.finatra.request.RouteParam

/**
  * Collection of data structures used in webservie API.
  * This is the way we model JSON structure, actually (thanks to the fact that Finatra maps case classes to JSON automatically).
  */
object Webservice {

  case class DeviceSimpleInfo(
    deviceType: String,
    uriPath: String,
    serialNumber: DeviceUniqueId
  )

  abstract class DeviceInfo {
  }

  case class Bridge(
    uriPath: String,
    model: String,
    name: String,
    serialNumber: String,
    firmwareVersion: String,
    ip: String,
    macAddress: String,
    lightsConnected: Int,
    sensorsConnected: Int
  ) extends DeviceInfo

  case class Light(
    uriPath: String,
    state: LightState,
    name: String,
    modelId: String,
    uniqueId: String,
    swVersion: String
  ) extends DeviceInfo

  case class Sensor(
    uriPath: String,
    name: String,
    modelId: String,
    uniqueId: String,
    swVersion: String,
    quantity: String,
    measuredValue: Double
  ) extends DeviceInfo

  case class LightState(
    on: Boolean,
    brightness: Int,
    hue: Int,
    saturation: Int,
    xy: (Double, Double),
    ct: Int,
    alert: String,
    effect: String,
    reachable: Boolean
  )

  case class LightAdjustment(
    @RouteParam bridgeId: String,
    @RouteParam lightId: String,
    on: Boolean,
    brightness: Int,
    hue: Option[Int],
    saturation: Option[Int],
    xy: Option[(Double, Double)],
    ct: Option[Int],
    effect: String
  )

  case class Error(errorCode: String, description: String, diagnosticInfo: Option[String])
}
