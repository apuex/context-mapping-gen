# Context-map generator

Generate code for context-mapping from between micro-services(bounded-contexts).

## Goals

Generate solution for
- subscribe data stream.
- data transformation.

## Usage

1. build generator from source.

```
$ git clone https://github.com/apuex/context-mapping-gen
$ cd context-mapping-gen
$ sbt lagom/assembly
```

2. run generator.

```
$ java -Doutput.dir=lagom/target/generated \
    -jar lagom/target/scala-2.12/context-mapping-gen-lagom-1.0.0.jar \
    generate-context-mapping \
    lagom/src/test/resources/com/github/apuex/ctxmapgen/lagom/mappings.xml 
```

replace `mappings.xml` with your own.

3. add additional google protobuf message schema file(s), if you have any,

```
$ mkdir -p lagom/target/generated/bc1-to-bc2/api/src/main/protobuf
$ cp ../akka-model-gen/examples/mappings.proto lagom/target/generated/bc1-to-bc2/api/src/main/protobuf
```

or add ready made protobuf message jar(s) to `lagom/target/generated/bc1-to-bc2/api/build.sbt` dependencies.

4. build generated code to runnable jar.

```
$ cd lagom/target/generated/bc1-to-bc2
$ sbt app/assembly
```

5. run.

```
$ java -jar app/target/scala-2.12/bc1-to-bc2-app-assembly-1.0.0.jar
``` 

## Build for Production

The `app` project is just for development purpose, 
and use leveldb for persistence, 
which is not recommended for production. 

Apache Cassandra is the preferred production database.

To create application package for production, copy the `app` project as the basis for your new project, 
maybe `prod-app`, you name it. And the make your changes to `prod-app/conf/application.conf` and `prod-app/build.sbt`,
and add to the root project's `build.sbt`, and finally, build.

```
$ sbt prod-app/assembly
```

## Contribute to This Project
