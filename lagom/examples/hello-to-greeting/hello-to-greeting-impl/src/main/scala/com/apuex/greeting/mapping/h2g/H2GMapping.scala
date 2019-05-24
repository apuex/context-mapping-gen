package com.apuex.greeting.mapping.h2g

import akka.Done
import akka.actor._
import akka.persistence._
import akka.stream._
import akka.stream.scaladsl._
import com.example.leveldb._
import com.github.apuex.events.play.EventEnvelope

import scala.concurrent.Await
import scala.concurrent.duration._

object H2GMapping {
  def name = "H2GMapping"
}

class H2GMapping (
                            config: MappingConfig,
                            hello: HelloService
                          )
  extends PersistentActor
    with ActorLogging {

  import config._

  implicit val executionContext = context.system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def persistenceId: String = H2GMapping.name

  // state
  var offset: Option[String] = None

  override def receiveRecover: Receive = {
    case SnapshotOffer(metadata: SnapshotMetadata, snapshot: String) =>
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
      mappingEvent(x.offset)(unpack(x.getEvent))
    case x =>
      log.info("unhandled command: {}", x)
  }

  private def updateState: (Any => Unit) = {
    case x: String =>
      offset = Some(x)
      if (lastSequenceNr != 0 && lastSequenceNr % snapshotSequenceCount == 0) {
        offset.map(saveSnapshot)
      }
    case x =>
      log.info("unhandled update state: {}", x)
  }

  private def mappingEvent(offset: String): (Any => Unit) = {
    case x: SayHelloEvent =>
      Await.ready(hello.echo(x.message)
        .invoke()
        .map(x => {
          log.info("message echoed back({}): {}", offset, x)
        }), 20.seconds)

      persist(offset)(updateState)
    case x =>
      log.debug("unhandled: {}", x)
  }

  private def subscribe(): Unit = {
    log.info("connecting...")
    hello.events(offset)
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
