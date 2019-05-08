package com.apuex.greeting.mapping.h2g;


import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.api.ServiceCall;

import javax.inject.Inject;
import java.util.Date;
import java.util.Optional;

import static java.util.concurrent.CompletableFuture.completedFuture;

class H2GMappingServiceImpl implements H2GMappingService {
  @Inject
  private MappingConfig config;

  /**
   * Subscribe from event stream with offset.
   *
   * @param offset timed-uuid or sequence specifies start position
   * @return
   */
  @Override
  public ServiceCall<Source<String, NotUsed>, Source<String, NotUsed>> events(Optional<String> offset) {
    System.out.println(offset);
    return inputs -> completedFuture(
        inputs.map(x -> String.format("[%s] - %s", new Date(), x))
    );
  }

}
