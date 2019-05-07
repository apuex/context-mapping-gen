package com.apuex.sales.mapping.bc1ToBc2

import java.util.Optional

import akka.Done
import akka.actor._
import akka.persistence._
import akka.persistence.query.{Offset, Sequence, TimeBasedUUID}
import akka.stream._
import akka.stream.scaladsl._
import com.github.apuex.events.play.EventEnvelope
import com.google.protobuf.Message
import javax.inject._

import scala.concurrent.duration._

object OrderInventoryMapping {
  def name = "OrderInventoryMapping"
}

class OrderInventoryMapping @Inject()(
                                       config: MappingConfig,
                                       product: ProductService,
                                       order: OrderService,
                                       inventory: InventoryService
                                     )
  extends PersistentActor
    with ActorLogging {

  import config._

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
      log.info("recover complete.")
      subscribe
    case x =>
      updateState(x)
  }

  override def receiveCommand: Receive = {
    case x: EventEnvelope =>
      // TODO: process event
      mappingEvent(packager.unpack(x.getEvent))
      persist(x.getOffset)(updateState)
    case x =>
      log.info("unhandled command: {}", x)
  }

  private def updateState: (Any => Unit) = {
    case x: Offset =>
      offset = Some(x)
      log.info("unhandled update state: {}", x)
  }

  private def mappingEvent: (Message => Unit) = {
    case x =>
      log.debug("unhandled: {}", x)
  }

  private def subscribe: Unit = {
    log.info("connecting...")
    order.events(offsetToString(offset))
      .invoke(
        Source(Long.MinValue to Long.MaxValue)
          .throttle(1, 30.second)
          .map(_.toString)
          .asJava
      )
      .whenComplete((s, t) => {
        if (null == t) {
          log.info("connected.")
          s.asScala
            .map(parseJson)
            .recover({
              case x =>
                log.error(x, "broken pipe")
                context.system.scheduler.scheduleOnce(30.seconds)(subscribe)
            })
            .runWith(Sink.actorRef(self, Done))
        } else {
          log.error(t, "connect to event stream failed")
          context.system.scheduler.scheduleOnce(30.seconds)(subscribe)
        }
      })
  }

  private def parseJson(json: String): EventEnvelope = {
    val builder = EventEnvelope.newBuilder()
    parser.merge(json, builder)
    builder.build()
  }

  private def offsetToString(offset: Option[Offset]): Optional[String] = {
    Optional.of(offset.map({
      case Sequence(value) => value.toString
      case TimeBasedUUID(value) => value.toString
    }).getOrElse(""))
  }
}
