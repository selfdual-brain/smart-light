package com.selfdualbrain.finatrapoc

import com.selfdualbrain.finatrapoc.webservice.SmartLightServer
import com.twitter.finagle.http.Status
import com.twitter.finatra.http.test.EmbeddedHttpServer
import com.twitter.inject.server.FeatureTest

class SmartLightWebserviceIntegrationTest extends FeatureTest {

  override val server = new EmbeddedHttpServer(new SmartLightServer)

  "Server" should {

    "List connected bridges" in {
      server.httpGet(
        path = "/system/bridges",
        andExpect = Status.Ok)
    }

    "Return bridge B-001 info" in {
      server.httpGet(
        path = "/bridge/B-001",
        andExpect = Status.Ok)
    }

    "Return bridge B-002 info" in {
      server.httpGet(
        path = "/bridge/B-002",
        andExpect = Status.Ok)
    }

    "Refuse getting info on non-existing bridge B-003" in {
      server.httpGet(
        path = "/bridge/B-003",
        andExpect = Status.NotFound)
    }

    "List devices connected to bridge B-001" in {
      server.httpGet(
        path = "/bridge/B-001/devices",
        andExpect = Status.Ok)
    }

    "Reboot bridge B-001" in {
      server.httpPost(
        path = "/bridge/B-001/reboot",
        postBody = "",
        andExpect = Status.Ok
      )
    }

    "Return light L-03672178 info" in {
      server.httpGet(
        path = "/bridge/B-001/light/L-03672178",
        andExpect = Status.Ok
      )
    }

    "Adjust light L-03672178" in {
      server.httpPost(
        path = "/bridge/B-001/light/L-03672178/adjust",
        postBody =
          """
            |{
            |    "on": true,
            |    "brightness": 21,
            |    "hue": 0,
            |    "saturation": 74,
            |    "effect": "NONE"
            |}
            |
          """.stripMargin,
        andExpect = Status.Ok
      )
    }

    "Fail to adjust light L-03672178 when the underlying actor returns unexpected message" in {
      server.httpPost(
        path = "/bridge/B-001/light/L-03672178/adjust",
        postBody =
          """
            |{
            |    "on": true,
            |    "brightness": 21,
            |    "hue": 0,
            |    "saturation": 74,
            |    "effect": "BLOW-THE-SYSTEM"
            |}
            |
          """.stripMargin,
        andExpect = Status.InternalServerError
      )
    }

    "Fail to adjust light L-03672178 when the brightness is outside supported range" in {
      server.httpPost(
        path = "/bridge/B-001/light/L-03672178/adjust",
        postBody =
          """
            |{
            |    "on": true,
            |    "brightness": 300,
            |    "hue": 0,
            |    "saturation": 74,
            |    "effect": "NONE"
            |}
            |
          """.stripMargin,
        andExpect = Status.UnprocessableEntity
      )
    }

    "Fail to adjust light L-03672178 when the underlying actor returns no message" in {
      server.httpPost(
        path = "/bridge/B-001/light/L-03672178/adjust",
        postBody =
          """
            |{
            |    "on": true,
            |    "brightness": 300,
            |    "hue": 0,
            |    "saturation": 74,
            |    "effect": "BE-DEAF"
            |}
            |
          """.stripMargin,
        andExpect = Status.InternalServerError
      )
    }

    "Switch on light L-03672178" in {
      server.httpPost(
        path = "/bridge/B-001/light/L-03672178/switch-on",
        postBody = "",
        andExpect = Status.Ok
      )
    }

    "Switch off light L-03672178" in {
      server.httpPost(
        path = "/bridge/B-001/light/L-03672178/switch-off",
        postBody = "",
        andExpect = Status.Ok
      )
    }

    "Return info for sensor S-40022" in {
      server.httpGet(
        path = "/bridge/B-001/sensor/S-40022",
        andExpect = Status.Ok
      )
    }

  }
}
