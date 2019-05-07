package com.apuex.sales.mapping.bc1ToBc2;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import java.util.Optional;

import com.lightbend.lagom.javadsl.api.Service;
import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.pathCall;

public interface InventoryService extends Service {


  ServiceCall<ReduceStorageCmd, Done> reduce();

  /**
   * Subscribe from event stream with offset.
   *
   * @param offset timed-uuid or sequence specifies start position
   * @return
   */
  ServiceCall<akka.stream.javadsl.Source<String, NotUsed>, akka.stream.javadsl.Source<String, NotUsed>> events(Optional<String> offset);

  @Override
  default Descriptor descriptor() {
    return named("inventory")
        .withCalls(
            pathCall("/api/events?offset", this::events),
            pathCall("/api/reduce-storage", this::reduce)
        ).withAutoAcl(true);
  }
}
