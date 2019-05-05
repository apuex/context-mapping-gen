package com.apuex.sales.mapping.bc1ToBc2

import akka.actor._
import akka.pattern._
import akka.persistence._
import akka.persistence.query._
import akka.stream._
import akka.stream.scaladsl._
import javax.inject._

object OrderInventoryMapping {
  def name = "OrderInventoryMapping"
}
       
class OrderInventoryMapping @Inject()(order: ActorRef, @Named("inventory") inventory: ActorRef)
  extends PersistentActor
    with ActorLogging {
  implicit val executionContext = context.system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def persistenceId: String = OrderInventoryMapping.name

  // state
  var lastEvent: Option[EventEnvelope] = None
         
  override def receiveRecover: Receive = {
    case SnapshotOffer(metadata: SnapshotMetadata, snapshot: EventEnvelope) =>
      lastEvent = Some(snapshot)
    case _: RecoveryCompleted =>
      // TODO: connect to source service and start event subscription.
    case x =>
      updateState(x)
  }

  override def receiveCommand: Receive = {
    case x: EventEnvelope =>
      // TODO: process event
      persist(x)(updateState)
    case x =>
      log.info("unhandled command: {}", x)
  }

  private def updateState: (Any => Unit) = {
    case x: EventEnvelope =>
      lastEvent = Some(x)
      log.info("unhandled update state: {}", x)
  }

}
