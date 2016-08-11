package com.selfdualbrain.finatrapoc

import com.google.inject.Stage
import com.selfdualbrain.finatrapoc.webservice.SmartLightServer
import com.twitter.finatra.http.test.EmbeddedHttpServer
import com.twitter.inject.server.FeatureTest

class SmartLightServerStartupTest extends FeatureTest {

  override val server = new EmbeddedHttpServer(
    twitterServer = new SmartLightServer,
    stage = Stage.PRODUCTION,
    verbose = false)

  "Server" should {
    "startup" in {
      server.assertHealthy()
    }
  }
}
