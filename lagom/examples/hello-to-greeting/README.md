# Bounded Context 1 to Bounded Context 2 Mapping Example

## How to Run

```
$ git clone https://github.com/apuex/context-mapping-gen
$ cd context-mapping-gen/lagom/examples/hello-to-greeting
$ sbt hello-to-greeting-app/assembly
$ java -DHOSTNAME=localhost -DSEED_NODE=localhost -jar hello-to-greeting-app/target/scala-2.12/hello-to-greeting-app-assembly-1.0.0.jar 
```

