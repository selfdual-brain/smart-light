package com.selfdualbrain.finatrapoc.webservice

import akka.actor.{ActorNotFound, ActorRef, ActorSystem}
import akka.pattern.{AskTimeoutException, ask}
import akka.util.Timeout
import com.google.inject.Inject
import com.google.inject.name.Named
import com.selfdualbrain.finatrapoc.core.actors.{BridgeActor, LightActor, LightSystemManagementActor, SensorActor}
import com.selfdualbrain.finatrapoc.core.config.Config
import com.selfdualbrain.finatrapoc.core.model._
import com.selfdualbrain.finatrapoc.utils.TwitterFutureConverters
import com.twitter.finagle.http.{Status, Request, Response}
import com.twitter.finatra.http.Controller

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

/**
  * Light system webservice controllers.
  * This is where the webservice is actually defined.
  * Mapping to AKKA layer is also happening here.
  */
class SmartLightController @Inject() (actorSystem: ActorSystem, @Named("root-actor") rootActor: ActorRef) extends Controller {

//============================================= WEBSERVICE REQUEST PATTERNS AND THEIR AKKA MAPPING ===================================================

//################## WEBSERVICE LAYER ######    ######################################################## AKKA LAYER #############################################################################
//                                               --------------target actor------------  ------------------ request message -----------------  ------------- response message -------------------
//-------------- SYSTEM --------------------
//  GET  /system/bridges                          /root-manager                           LightSystemManagementActor.Req.ListConnectedBridges   LightSystemManagementActor.Res.ConnectedBridges

//-------------- BRIDGE --------------------
//  GET  /bridge/0203                             /root-manager/bridge-0203               BridgeActor.Req.GetDeviceInfo                         BridgeActor.Res.DeviceInfo
//  GET  /bridge/0120/devices                     /root-manager/bridge-0120               BridgeActor.Req.GetDeviceInfo                         BridgeActor.Res.DeviceInfo
//  POST /bridge/00224/reboot                     /root-manager/bridge-00224              BridgeActor.Req.Restart                               (NONE)

//-------------- LIGHT ---------------------
//  GET  /bridge/0120/light/18054                 /root-manager/bridge-0120/light-18054   LightActor.Req.GetDeviceInfo                          LightActor.Res.DeviceInfo
//  POST /bridge/0120/light/00232/adjust          /root-manager/bridge-0120/light-00232   LightActor.Req.AdjustLight                            LightActor.Res.(DeviceInfo, FeatureNotSupported)
//  POST /bridge/0120/light/00223/switch-on       /root-manager/bridge-0120/light-00223   LightActor.Req.SwitchOn                               (NONE)
//  POST /bridge/0120/light/00223/switch-off      /root-manager/bridge-0120/light-00223   LightActor.Req.SwitchOff                              (NONE)

//-------------- SENSOR --------------------
//  GET  bridge/0120/sensor/1422                 /root-manager/bridge-0120/sensor-1422   LightActor.Req.GetDeviceInfo                          SensorActor.Res.SensorInfo


//========================= REQUEST: GET /system/bridges ==============================================================================================

  get("/system/bridges") { request: Request =>
    debug(s"WEBSERVICE: ${request.method} ${request.uri}")

    val convertReplyMsg: LightSystemManagementActor.Res.ConnectedBridges => Iterable[Webservice.DeviceSimpleInfo] = { msg =>
      for {(bridgeSerialNumber, actorRef) <- msg.map}
      yield Webservice.DeviceSimpleInfo(deviceType = "bridge", uriPath = WsResourcePaths.bridge(bridgeSerialNumber), serialNumber = bridgeSerialNumber)
    }

    akkaProcessing_Ask(
      actorPath = ActorPaths.rootManager,
      sendMsg = LightSystemManagementActor.Req.ListConnectedBridges,
      responseConverter = {
        case msg: LightSystemManagementActor.Res.ConnectedBridges => response.ok(convertReplyMsg(msg))
      }
    )
  }

//========================= REQUEST: GET /bridge/0203 =================================================================================================

  get("/bridge/:id") { request: Request =>
    debug(s"WEBSERVICE: ${request.method} ${request.uri}")

    val bridgeId = DeviceUniqueId(request.params("id"))

    val convertReplyMsg: BridgeActor.Res.DeviceInfo => Webservice.DeviceInfo = {msg =>
      Webservice.Bridge(
        uriPath = WsResourcePaths.bridge(msg.info.serialNumber),
        model = msg.info.model.value,
        name = msg.info.name,
        serialNumber = msg.info.serialNumber.value,
        firmwareVersion = msg.info.firmware.value,
        ip = msg.info.internalIpAddress,
        macAddress = msg.info.macAddress,
        lightsConnected = msg.lights.size,
        sensorsConnected = msg.sensors.size
      )
    }

    akkaProcessing_Ask(
      actorPath = ActorPaths.bridge(bridgeId),
      sendMsg = BridgeActor.Req.GetDeviceInfo,
      responseConverter = {
        case msg: BridgeActor.Res.DeviceInfo => response.ok(convertReplyMsg(msg))
      }
    )
  }

//========================= REQUEST: GET /bridge/0120/devices ==========================================================================================

  get("/bridge/:id/devices") { request: Request =>
    debug(s"WEBSERVICE: ${request.method} ${request.uri}")

    val bridgeId = DeviceUniqueId(request.params("id"))

    val convertReplyMsg: BridgeActor.Res.DeviceInfo => Iterable[Webservice.DeviceSimpleInfo] = {msg =>
      val lights = for {(id, actorRef) <- msg.lights}
        yield Webservice.DeviceSimpleInfo(
          deviceType = "light",
          uriPath = WsResourcePaths.light(bridge = msg.info.serialNumber, light = id),
          serialNumber = id
        )

      val sensors = for {(id, actorRef) <- msg.sensors}
        yield Webservice.DeviceSimpleInfo(
          deviceType = "sensor",
          uriPath = WsResourcePaths.sensor(bridge = msg.info.serialNumber, sensor = id),
          serialNumber = id
        )

      lights ++ sensors
    }

    akkaProcessing_Ask(
      actorPath = ActorPaths.bridge(bridgeId),
      sendMsg = BridgeActor.Req.GetDeviceInfo,
      responseConverter = {
        case msg: BridgeActor.Res.DeviceInfo => response.ok(convertReplyMsg(msg))
      }
    )
  }

//========================= REQUEST: POST /bridge/00224/reboot ==========================================================================================

  post("/bridge/:id/reboot") { request: Request =>
    debug(s"WEBSERVICE: ${request.method} ${request.uri}")

    val bridgeId = DeviceUniqueId(request.params("id"))

    akkaProcessing_Tell(
      actorPath = ActorPaths.bridge(bridgeId),
      sendMsg = BridgeActor.Req.Restart
    )
  }

//========================= REQUEST: GET /bridge/0120/light/18054 ==========================================================================================

  get("/bridge/:bridgeId/light/:lightId") { request: Request =>
    debug(s"WEBSERVICE: ${request.method} ${request.uri}")

    val bridgeId = DeviceUniqueId(request.params("bridgeId"))
    val lightId = DeviceUniqueId(request.params("lightId"))

    akkaProcessing_Ask(
      actorPath = ActorPaths.light(bridgeId, lightId),
      sendMsg = LightActor.Req.GetDeviceInfo,
      responseConverter = {
        case msg: LightActor.Res.DeviceInfo => response.ok(convertLightDeviceInfo(bridgeId, msg))
      }
    )
  }

  def convertLightDeviceInfo(bridgeId: DeviceUniqueId, msg: LightActor.Res.DeviceInfo): Webservice.Light = {
    val cieColor = msg.state.color.convertToCie
    val hsvColor = msg.state.color.convertToHSV
    val tempColor = msg.state.color.convertToColorTemp

    val lightState = Webservice.LightState(
      on = msg.state.onOffStatus == OnOffStatus.On,
      brightness = msg.state.brightness,
      hue = hsvColor.hue,
      saturation = hsvColor.saturation,
      xy = (cieColor.x, cieColor.y),
      ct = tempColor.t,
      alert = "NONE",
      effect = "NONE",
      reachable = msg.isConnected
    )

    Webservice.Light(
      uriPath = WsResourcePaths.light(bridgeId, msg.info.serialNumber),
      state = lightState,
      name = msg.info.name,
      modelId = msg.info.model.value,
      uniqueId = msg.info.serialNumber.value,
      swVersion = msg.info.firmware.value
    )
  }

  //========================= REQUEST: POST /bridge/0120/light/00232/adjust ==========================================================================================

  post("/bridge/:bridge_id/light/:light_id/adjust") { request: Webservice.LightAdjustment =>
    debug(s"WEBSERVICE: /bridge/${request.bridgeId}/light/${request.lightId}/adjust")

    val bridgeId = DeviceUniqueId(request.bridgeId)
    val lightId = DeviceUniqueId(request.lightId)

    val requestedColor: Color =
      if (request.hue.isDefined)
        HSV(request.hue.get, request.saturation.get)
      else if (request.xy.isDefined)
        CieColorPoint(request.xy.get._1, request.xy.get._2)
      else if (request.ct.isDefined)
        ColorTemp(request.ct.get)
      else throw new Exception("color value is missing")

    akkaProcessing_Ask(
      actorPath = ActorPaths.light(bridgeId, lightId),
      sendMsg = LightActor.Req.AdjustLight(brighness = request.brightness, color = requestedColor, effect = request.effect),
      responseConverter = {
        case msg: LightActor.Res.DeviceInfo => response.ok(convertLightDeviceInfo(bridgeId, msg))
        case LightActor.Res.EffectNotSupported(effectName) =>
          response.status(Status.UnprocessableEntity).body(Webservice.Error(
            errorCode = "effect-not-supported",
            description = s"effect $effectName is not supported for light device ${request.lightId}",
            diagnosticInfo = None))
        case LightActor.Res.BrightnessOutsideSupportedRange(value, supportedRangeMin, supportedRangeMax) =>
          response.status(Status.UnprocessableEntity).body(Webservice.Error(
            errorCode = "brightness-outside-supported-range",
            description = s"brightness value $value was outside the supported range: ($supportedRangeMin ... $supportedRangeMax)",
            diagnosticInfo = None
          ))
      }
    )
  }

//========================= REQUEST: POST /bridge/0120/light/00232/switch-on ==========================================================================================

  post("/bridge/:bridgeId/light/:lightId/switch-on") { request: Request =>
    debug(s"WEBSERVICE: ${request.method} ${request.uri}")

    val bridgeId = DeviceUniqueId(request.params("bridgeId"))
    val lightId = DeviceUniqueId(request.params("lightId"))

    akkaProcessing_Tell(
      actorPath = ActorPaths.light(bridgeId, lightId),
      sendMsg = LightActor.Req.SwitchOn
    )
  }

//========================= REQUEST: POST /bridge/0120/light/00232/switch-off ==========================================================================================

  post("/bridge/:bridgeId/light/:lightId/switch-off") { request: Request =>
    debug(s"WEBSERVICE: ${request.method} ${request.uri}")

    val bridgeId = DeviceUniqueId(request.params("bridgeId"))
    val lightId = DeviceUniqueId(request.params("lightId"))

    akkaProcessing_Tell(
      actorPath = ActorPaths.light(bridgeId, lightId),
      sendMsg = LightActor.Req.SwitchOff
    )
  }

  //========================= REQUEST: GET bridge/0120/sensor/1422 ==========================================================================================

  get("/bridge/:bridgeId/sensor/:sensorId") { request: Request =>
    debug(s"WEBSERVICE: ${request.method} ${request.uri}")

    val bridgeId = DeviceUniqueId(request.params("bridgeId"))
    val sensorId = DeviceUniqueId(request.params("sensorId"))

    val convertReplyMsg: SensorActor.Res.DeviceInfo => Webservice.Sensor = {msg =>
      Webservice.Sensor(
        uriPath = WsResourcePaths.sensor(bridgeId, msg.info.serialNumber),
        name = msg.info.name,
        modelId = msg.info.model.value,
        uniqueId = msg.info.serialNumber.value,
        swVersion = msg.info.firmware.value,
        quantity = msg.info.quantity.toString,
        measuredValue = msg.measuredValue
      )
    }

    akkaProcessing_Ask(
      actorPath = ActorPaths.sensor(bridgeId, sensorId),
      sendMsg = SensorActor.Req.GetDeviceInfo,
      responseConverter = {
        case msg: SensorActor.Res.DeviceInfo => response.ok(convertReplyMsg(msg))
      }
    )
  }

//================================================== FINATRA - AKKA INTEGRATION (NAIVE IMPLEMENTATION) =========================================================

  /**
    * Finatra-Akka bridge for the "send msg and wait for response" behaviour.
    * This is a very simple implementation which shows the main trick of Finatra-Akka integration.
    * To keep things simple, this implementation handles only the "happy path" (no errors handling here).
    *
    * We are not using this one - it is left here just to illustrate the main idea.
    * Instead we use the more sophisticated version (see "akkaProcessing_Ask") where all corner cases are handled.
    */
  def akkaProcessing_Ask_NaiveImplementation[R,T](actorPath: String, sendMsg: Any, responseConverter: R => T): com.twitter.util.Future[T] = {
    implicit val timeout: Timeout = 5 seconds
    implicit val executionContext = actorSystem.dispatcher

    val fut: Future[T] = for {
      targetActor <- actorSystem.actorSelection(actorPath).resolveOne
      responseMsg <- targetActor ? sendMsg
    }
      yield responseConverter(responseMsg.asInstanceOf[R])


    return TwitterFutureConverters.scalaToTwitterFuture(fut)(actorSystem.dispatcher)
  }

  /**
    * Finatra-Akka bridge for the "send msg and forget" behaviour.
    *
    * @param actorPath
    * @param sendMsg
    * @return
    */
  def akkaProcessing_Tell_NaiveImplementation(actorPath: String, sendMsg: Any): com.twitter.util.Future[Unit] = {
    implicit val timeout: Timeout = 5 seconds
    implicit val executionContext = actorSystem.dispatcher

    val fut = actorSystem.actorSelection(actorPath).resolveOne
    fut onSuccess {case targetActor => targetActor ! sendMsg}
    val futWithIgnoredResult = fut map (x => ())
    return TwitterFutureConverters.scalaToTwitterFuture(futWithIgnoredResult)(actorSystem.dispatcher)
  }


  /**
    * Akka processing request-response, implemented as sequential (blocking) processing. Useful for debugging.
    */
  def akkaProcessingBlocking[R,T](actorPath: String, sendMsg: Any, expectResponse: Class[R], responseConverter: R => T): T = {
    implicit val timeout: Timeout = 5 seconds
    implicit val executionContext = actorSystem.dispatcher

    debug(s"resolving actorpath: $actorPath")
    val targetActor = Await.result(actorSystem.actorSelection(actorPath).resolveOne, 2 seconds)
    debug(s"successfully resolved actor path to: $targetActor")

    val responseMsg = Await.result(targetActor ? sendMsg, 5 seconds)
    if (expectResponse.isInstance(responseMsg)) {
      debug(s"ready to invoke converter on reeived message: $responseMsg")
      val converted = responseConverter(responseMsg.asInstanceOf[R])
      debug(s"conversion done, result is: $converted")
      converted
    }

    else
      throw new Exception(s"unexpected type of response message received from akka layer, expected was $expectResponse, got ${responseMsg.getClass}")
  }

//================================================== FINATRA - AKKA INTEGRATION (FULLY-FEATURED IMPLEMENTATION) =========================================================

  def akkaProcessing_Ask(actorPath: String, sendMsg: Any, responseConverter: PartialFunction[Any,Response]): com.twitter.util.Future[Response] = {
    implicit val timeout: Timeout = Config.finatraAkkaBridgeAskTimeout
    implicit val executionContext = actorSystem.dispatcher

    val futureActorRef: Future[ActorRef] = actorSystem.actorSelection(actorPath).resolveOne(Config.finatraAkkaBridgePathResolvingTimeout)
    val handlerOfUnexpetedMsgGotFromActor: PartialFunction[Any, Response] = {case msg =>
      response.internalServerError(Webservice.Error(
        errorCode = "unexpected-msg-from-actor",
        description = "server-side problem, please contact the service team",
        diagnosticInfo = Some(s"actor path = $actorPath, received msg = $msg")
      ))}

    val responseConverterWithUnexpectedMsgHandling: Any => Response = responseConverter orElse handlerOfUnexpetedMsgGotFromActor

    val scalaFutureWithAllCasesHandled: Future[Response] = (futureActorRef flatMap {actorRef => (actorRef ? sendMsg) map responseConverterWithUnexpectedMsgHandling}) recover {
      case ex: ActorNotFound => response.notFound(Webservice.Error(
        errorCode = "not such resource",
        description = "the URI pointed to non-existing resource, this is client-side error",
        diagnosticInfo = Some(s"request internally converted to akka actor path: $actorPath, but such actor was not found in the system")
      ))
      case ex: AskTimeoutException => response.internalServerError(Webservice.Error(
        errorCode = "finatra-akka-bridge-level-akka-ask-timeout",
        description = "server-side problem, please contact the service team; this may be a server-too-busy side efect",
        diagnosticInfo = Some(s"actor path = $actorPath, sent msg = $sendMsg,  timeout is currently configured to: ${Config.finatraAkkaBridgeAskTimeout}")
      ))
    }

    return TwitterFutureConverters.scalaToTwitterFuture(scalaFutureWithAllCasesHandled)(actorSystem.dispatcher)
  }

  def akkaProcessing_Tell(actorPath: String, sendMsg: Any): com.twitter.util.Future[Response] = {
    implicit val executionContext = actorSystem.dispatcher

    val futureActorRef: Future[ActorRef] = actorSystem.actorSelection(actorPath).resolveOne(Config.finatraAkkaBridgePathResolvingTimeout)

    val fut = (futureActorRef map {targetActorRef => targetActorRef ! sendMsg; response.ok}) recover {
      case ex: ActorNotFound => response.notFound(Webservice.Error(
        errorCode = "not such resource",
        description = "the URI pointed to non-existing resource, this is client-side error",
        diagnosticInfo = Some(s"request internally converted to akka actor path: $actorPath, but such actor was not found in the system")
      ))
    }

    return TwitterFutureConverters.scalaToTwitterFuture(fut)(actorSystem.dispatcher)
  }

}
