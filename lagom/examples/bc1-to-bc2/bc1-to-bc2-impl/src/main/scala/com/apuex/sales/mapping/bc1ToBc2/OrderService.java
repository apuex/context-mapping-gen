package com.apuex.sales.mapping.bc1ToBc2;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;

import java.util.Optional;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.pathCall;

public interface OrderService extends Service {

  ServiceCall<RetrieveOrderCmd, OrderVo> retrieve();

  /**
   * Subscribe from event stream with offset.
   *
   * @param offset timed-uuid or sequence specifies start position
   * @return
   */
  ServiceCall<akka.stream.javadsl.Source<String, NotUsed>, akka.stream.javadsl.Source<String, NotUsed>> events(Optional<String> offset);

  @Override
  default Descriptor descriptor() {
    return named("order")
        .withCalls(
            pathCall("/api/events?offset", this::events),
            pathCall("/api/retrieve-order", this::retrieve)
        ).withAutoAcl(true);
  }
}
