package com.apuex.sales.mapping.bc1ToBc2Mapping.impl

import akka._
import akka.actor._
import akka.persistence._
import akka.stream._
import akka.stream.scaladsl._
import com.apuex.sales.mapping.bc1ToBc2Mapping._
import com.github.apuex.events.play._

import scala.concurrent._

object Bc1ToBc2Mapping {
  def name = "Bc1ToBc2Mapping"
}

class Bc1ToBc2Mapping(
                       val config: MappingConfig,
                       val order: OrderService,
                       val product: ProductService,
                       val inventory: InventoryService
                     )
  extends PersistentActor
    with ActorLogging {

  import config._

  implicit val executionContext = context.system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def persistenceId: String = Bc1ToBc2Mapping.name

  // state
  var offset: Option[String] = None

  override def receiveRecover: Receive = {
    case SnapshotOffer(metadata: SnapshotMetadata, snapshot: String) =>
      offset = Some(snapshot)
    case _: RecoveryCompleted =>
      // TODO: connect to source service and start event subscription.
      log.info("recover complete.")
      subscribe()
    case x =>
      updateState(x)
  }

  override def receiveCommand: Receive = {
    case x: EventEnvelope =>
      // TODO: process event
      mapEvent(x.offset)(unpack(x.getEvent))
    case x =>
      log.info("unhandled command: {}", x)
  }

  private def updateState: (Any => Unit) = {
    case x: String =>
      offset = Some(x)
      log.info("unhandled update state: {}", x)
  }

  private def mapEvent(offset: String): (Any => Unit) = {
    // TODO: add message mappings here.
    case evt: PayOrderEvt =>
      Await.ready(order.retrieve().invoke(RetrieveOrderCmd(evt.orderId))
        .map(x => x.items
          .map(item => product.retrieve().invoke(RetrieveProductCmd(item.productId))
            .map(x => inventory.reduce().invoke(ReduceStorageCmd(x.sku, item.quantity))))
        )
        .map(_ => {
          persist(offset)(updateState)
        }), duration)
    case x =>
      log.debug("unhandled: {}", x)
  }

  private def subscribe(): Unit = {
    log.info("connecting...")
    order.events(offset)
      .invoke(
        keepAlive
      )
      .map(s => {
        log.info("connected.")
        s.map(parseJson)
          .recover({
            case x =>
              log.error(x, "broken pipe")
              context.system.scheduler.scheduleOnce(duration)(subscribe)
          }).runWith(Sink.actorRef(self, Done))
      })
      .recover({
        case t: Throwable =>
          log.error(t, "connect to event stream failed")
          context.system.scheduler.scheduleOnce(duration)(subscribe)
      })
  }

}
