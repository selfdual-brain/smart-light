package com.selfdualbrain.finatrapoc.core.config

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Hardcoded config (simple enough for this proof-of-concept).
  * In a production system it may be placed in application.conf or elsewhere (in guice config ?)
  */
object Config {
  val sensorsPollingInterval = 10 seconds
  val sensorsPrecision = 0.5
  val finatraAkkaBridgePathResolvingTimeout = 2 seconds
  val finatraAkkaBridgeAskTimeout = 5 seconds

}
