package com.selfdualbrain.finatrapoc.utils

import akka.actor.{Actor, ActorRef, Terminated}

import scala.collection.mutable

/**
  * Simple events broadcasting (aka pub-sub model) support.
  * To be mixed into actor classes.
  *
  * This event broadacting mechanism is quite different than akka build-in pub-sub solution (knowns as "akka event buses")
  * and should be considered an alternative approach.
  * We just allow any actor to broadcast events (as messages) and any other actors to subscribe.
  * So the idea follows rather the "Smalltalk" tradition of events broadcasting.
  *
  * There is also the idea of events forwarding or re-broadcasting - some actor may decide to re-breadcast events got from
  * other actors, effectively forming a logical tree of event sources.
  */
trait EventsBroadcaster {
  thisActor: Actor =>

  import EventsBroadcaster._

  private val subscribers: mutable.Set[ActorRef] = new mutable.HashSet[ActorRef]
  private val actorsObservedForEventsReBroadcasting: mutable.Set[ActorRef] = new mutable.HashSet[ActorRef]

  /**
    * I execute broadcast of provided event to all my current subscribers.
    * Broadcasted event is packed into EventEnvelope.
    *
    * @param msg event to be broadcasted
    */
  protected def trigger(msg: Any): Unit = {
    for (actor <- subscribers)
      actor ! EventEnvelope(self, msg)
  }

  /**
    * Because I consume Akka built-in "Terminated" message (for watching death of subscribers)
    * this inteterferes with Akka deathwatch mechanism.
    *
    * If any broadcasting-enabled actor wants to be informed of a death of another actor,
    * it should subscribe by context.watch(actor)
    * and then override this method (instead of handling Terminated message itself)
    *
    * @param actor reference to actor that just stopped
    */
  protected def actorTerminatedHandler(actor: ActorRef): Unit = {
    subscribers remove actor
    actorsObservedForEventsReBroadcasting remove actor
  }

  /**
    * Override this method to handle incoming events.
    *
    * @param origin actor which originally triggered this event (because of events forwarding, this is not necessarily the same actor we subscribed to)
    * @param payload the event itself
    */
  protected def handleEvent(origin: ActorRef, payload: Any): Unit = {
    //by default do nothing
  }

  /**
    * I subscribe myself for getting events broadcasted by specified target actor.
    * This is equivalent to sending Subscribe message to this target actor.
    */
  protected def listenToEventsFrom(eventsBroadcastingActor: ActorRef): Unit = {
    eventsBroadcastingActor ! Subscribe
  }

  /**
    * I subscribe myself for gettign events broadcasted by specified target actor.
    * Addidionally however I will be re-broadasting all incoming events to my subscribers.
    */
  protected def listenAndReBroadcastEventsFrom(actor: ActorRef) {
    actorsObservedForEventsReBroadcasting.add(actor)
    listenToEventsFrom(actor)
  }

  override def receive: Receive = {
    case Subscribe =>
      subscribers add sender
      context.watch(sender)

    case Unsubscribe =>
      subscribers remove sender
      context.unwatch(sender)

    case Terminated(actor) =>
      this.actorTerminatedHandler(actor)

    case event @ EventEnvelope(origin, payload) =>
      if (actorsObservedForEventsReBroadcasting.contains(sender))
        for (actor <- subscribers)
          actor ! event
      handleEvent(origin, payload)
  }
}

object EventsBroadcaster {
  /** Internal message for events transporting. */
  case class EventEnvelope(origin: ActorRef, payload: Any)
  /** A message sent from listener A to broadcaster B to signal the wish that A wants go get all events broadcasted by B. */
  case object Subscribe
  /** A message sent from listener A to broadcaster B to signal the wish that A is no longer interested in getting events from B. */
  case object Unsubscribe
}