package com.apuex.sales.mapping.bc1ToBc2;


import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.api.ServiceCall;

import javax.inject.Inject;
import java.util.Date;
import java.util.Optional;

import static java.util.concurrent.CompletableFuture.completedFuture;

class Bc1ToBc2MappingServiceImpl implements Bc1ToBc2MappingService {
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
        inputs.map(x -> String.format("[%s] - %s", new Date(),x))
    );
  }

}
