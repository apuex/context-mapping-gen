package com.apuex.sales.mapping.bc1ToBc2

import akka.Done
import akka.actor._
import akka.persistence._
import akka.persistence.query.Offset
import akka.stream._
import akka.stream.scaladsl._
import com.github.apuex.events.play.EventEnvelope
import com.google.protobuf
import com.google.protobuf.Message
import javax.inject._

import scala.concurrent.duration._

object OrderInventoryMapping {
  def name = "OrderInventoryMapping"
}

class OrderInventoryMapping @Inject()(
                                       config: MappingConfig,
                                       order: OrderService,
                                       inventory: InventoryService
                                     )
  extends PersistentActor
    with ActorLogging {
  implicit val executionContext = context.system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def persistenceId: String = OrderInventoryMapping.name

  // state
  var offset: Option[Offset] = None

  override def receiveRecover: Receive = {
    case SnapshotOffer(metadata: SnapshotMetadata, snapshot: Offset) =>
      offset = Some(snapshot)
    case _: RecoveryCompleted =>
      // TODO: connect to source service and start event subscription.
      subscribe
    case x =>
      updateState(x)
  }

  override def receiveCommand: Receive = {
    case x: EventEnvelope =>
      // TODO: process event
      mappingEvent(unpack(x.getEvent))
      persist(x.getOffset)(updateState)
    case x =>
      log.info("unhandled command: {}", x)
  }

  private def updateState: (Any => Unit) = {
    case x: Offset =>
      offset = Some(x)
      log.info("unhandled update state: {}", x)
  }

  def unpack(any: protobuf.Any): Message = {
    null
  }

  private def mappingEvent: (Message => Unit) = {
    case x =>
      log.debug("unhandled: {}", x)
  }

  private def subscribe: Unit = {
    order.events(offset.map(_.toString))
      .invoke(
        Source(Long.MinValue to Long.MaxValue)
          .throttle(1, 30.second)
          .map(_.toString)
      )
      .map(
        _.map(parseJson)
          .recover({
            case x =>
              log.error(x, "broken pipe")
              context.system.scheduler.scheduleOnce(30.seconds)(subscribe)
          })
          .runWith(Sink.actorRef(self, Done))
      ).recover({
      case x =>
        log.error(x, "connect to event stream failed")
        context.system.scheduler.scheduleOnce(30.seconds)(subscribe)
    })
  }

  private def parseJson(json: String): EventEnvelope = {
    val builder = EventEnvelope.newBuilder()
    config.parser.merge(json, builder)
    builder.build()
  }
}
