package com.apuex.greeting.mapping.h2g;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;

import java.util.Optional;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.pathCall;

public interface HelloService extends Service {

  ServiceCall<NotUsed, String> echo(String msg);

  ServiceCall<NotUsed, String> sayHello(String to);

  /**
   * Subscribe from event stream with offset.
   *
   * @param offset timed-uuid or sequence specifies start position
   * @return
   */
  ServiceCall<akka.stream.javadsl.Source<String, NotUsed>, akka.stream.javadsl.Source<String, NotUsed>> events(Optional<String> offset);

  @Override
  default Descriptor descriptor() {
    return named("hello")
        .withCalls(
            pathCall("/api/say-hello?to", this::sayHello),
            pathCall("/api/echo/:msg", this::echo),
            pathCall("/api/events?offset", this::events)
        ).withAutoAcl(true);
  }
}
