package com.selfdualbrain.finatrapoc.webservice

import com.selfdualbrain.finatrapoc.core.EngineModule
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters.{CommonFilters, LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.http.routing.HttpRouter

object SmartLightServerMain extends SmartLightServer

/**
  * Light system webservice server.
  * This is the entry point to the application.
  */
class SmartLightServer extends HttpServer {

  //  override val defaultFinatraHttpPort: String = ":8080"

  override val modules = Seq(EngineModule)

  override def configureHttp(router: HttpRouter) {
    router
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
      .filter[CommonFilters]
      .add[SmartLightController]
  }


}
