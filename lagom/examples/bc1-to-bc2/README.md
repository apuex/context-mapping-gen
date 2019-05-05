# Bounded Context 1 to Bounded Context 2 Mapping Example

## How to Run

```
$ git clone https://github.com/apuex/context-mapping-gen
$ cd context-mapping-gen/lagom/examples/bc1-to-bc2
$ sbt bc1-to-bc2-app/assembly
$ java -DHOSTNAME=localhost -DSEED_NODE=localhost -jar bc1-to-bc2-app/target/scala-2.12/bc1-to-bc2-app-assembly-1.0.0.jar 
```

