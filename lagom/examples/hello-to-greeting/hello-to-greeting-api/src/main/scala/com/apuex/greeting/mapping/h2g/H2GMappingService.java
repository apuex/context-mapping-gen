package com.apuex.greeting.mapping.h2g;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;

import java.util.Optional;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.pathCall;

public interface H2GMappingService extends Service {
  /**
    * Subscribe from event stream with offset.
    *
    * @param offset timed-uuid or sequence specifies start position
    * @return
    */
  ServiceCall<Source<String, NotUsed>, Source<String, NotUsed>>  events(Optional<String> offset);

  @Override
  default Descriptor descriptor() {
    return named("hello-to-greeting-mapping")
        .withCalls(
            pathCall("/api/events?offset", this::events)
        ).withAutoAcl(true);
  }
}
