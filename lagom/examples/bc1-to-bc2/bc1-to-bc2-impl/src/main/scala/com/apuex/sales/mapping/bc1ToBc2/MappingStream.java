package com.apuex.sales.mapping.bc1ToBc2;

import akka.actor.ActorSystem;
import akka.japi.pf.PFBuilder;
import akka.stream.javadsl.Source;
import scala.concurrent.ExecutionContext;

import javax.inject.Inject;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

public class MappingStream {
  @Inject
  private ActorSystem system;
  @Inject
  private ExecutionContext executor;
  @Inject
  private MappingConfig config;
  @Inject
  private InventoryService inventory;
  @Inject
  private OrderService order;
  @Inject
  private ProductService product;

  public void stream(Supplier<Optional<String>> offset) {
    order.events(offset.get())
        .invoke(
            config.keepAlive().asJava()
        )
        .whenComplete((is, throwable) -> {
          if (null == throwable)
            is.map(x -> config.parseJson(x))
                .map(x -> config.packager().unpack(x.getEvent()))
                .filter(x -> x instanceof PayOrderEvt)
                .map(x -> (PayOrderEvt) x)
                .mapAsync(8, x -> order.retrieve()
                    .invoke(
                        RetrieveOrderCmd.newBuilder()
                            .setOrderId(x.getOrderId())
                            .build()
                    )
                ).flatMapConcat(x -> Source.from(x.getItemsList()))
                .mapAsync(8, x -> product.retrieve()
                    .invoke(
                        RetrieveProductCmd.newBuilder()
                            .setProductId(x.getProductId())
                            .build()
                    )
                    .thenApply(p -> inventory.reduce()
                        .invoke(
                            ReduceStorageCmd.newBuilder()
                                .setSku(p.getSku())
                                .setQuantity(x.getQuantity())
                                .build()
                        )
                    )
                ).recover(new PFBuilder()
                .match(Throwable.class, ex -> system.scheduler().scheduleOnce(Duration.ofSeconds(30), () -> stream(offset), executor))
                .build()
            );
          else
            system.scheduler().scheduleOnce(Duration.ofSeconds(30), () -> stream(offset), executor);
        });
  }


}
