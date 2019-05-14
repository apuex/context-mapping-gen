package com.apuex.sales.mapping.bc1ToBc2

import java.util.concurrent.TimeUnit

import akka.Done
import akka.actor._
import akka.persistence._
import akka.stream._
import akka.stream.scaladsl._
import com.github.apuex.events.play.EventEnvelope
import com.google.protobuf.Message
import javax.inject._

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration._

object OrderInventoryMapping {
  def name = "OrderInventoryMapping"
}

class OrderInventoryMapping @Inject()(
                                       config: MappingConfig,
                                       order: OrderService,
                                       product: ProductService,
                                       inventory: InventoryService
                                     )
  extends PersistentActor
    with ActorLogging {

  import config._

  implicit val executionContext = context.system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def persistenceId: String = OrderInventoryMapping.name

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
      mappingEvent(x.getOffset)(packager.unpack(x.getEvent))
    case x =>
      log.info("unhandled command: {}", x)
  }

  private def updateState: (Any => Unit) = {
    case x: String =>
      offset = Some(x)
      log.info("unhandled update state: {}", x)
  }

  private def mappingEvent(offset: String): (Message => Unit) = {
    case x: PayOrderEvt =>
      Await.ready(order.retrieve()
        .invoke(
          RetrieveOrderCmd.newBuilder()
            .setOrderId(x.getOrderId())
            .build()
        ).map(o => o.getItemsList.asScala
        .map(i =>
          product.retrieve().invoke(
            RetrieveProductCmd.newBuilder()
              .setProductId(i.getProductId)
              .build()
          ).map(p =>
            inventory.reduce().invoke(
              ReduceStorageCmd.newBuilder()
                .setSku(p.getSku)
                .setQuantity(i.getQuantity)
                .build()
            )
          )
        )
        .foreach(x => Await.ready(x, Duration(30, TimeUnit.SECONDS)))
      ).map(_ => {
        persist(offset)(updateState)
      }), Duration(30, TimeUnit.SECONDS))
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
              context.system.scheduler.scheduleOnce(30.seconds)(subscribe)
          }).runWith(Sink.actorRef(self, Done))
      })
      .recover({
        case t: Throwable =>
          log.error(t, "connect to event stream failed")
          context.system.scheduler.scheduleOnce(30.seconds)(subscribe)
      })
  }
}
