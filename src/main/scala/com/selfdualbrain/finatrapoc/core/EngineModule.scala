package com.selfdualbrain.finatrapoc.core

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.name.Names
import com.selfdualbrain.finatrapoc.core.actors.LightSystemManagementActor
import com.selfdualbrain.finatrapoc.core.hal.SampleHardwareDefinition
import com.twitter.inject.TwitterModule

object EngineModule extends TwitterModule {

  override def configure(): Unit = {
    val actorSystem = ActorSystem("galaxy")

    bind[ActorSystem].toInstance(actorSystem)

    val hal = SampleHardwareDefinition.halSimulator
    val rootActor = actorSystem.actorOf(LightSystemManagementActor.props(hal), "root-manager")

    bind[ActorRef].annotatedWith(Names.named("root-actor")).toInstance(rootActor)
  }

}
