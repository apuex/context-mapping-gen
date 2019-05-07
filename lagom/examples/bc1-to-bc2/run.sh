#! /bin/bash
LAUNCHER_DIR=$(dirname $0) 
java -DHOSTNAME=localhost -DSEED_NODE=localhost -jar $LAUNCHER_DIR/bc1-to-bc2-app/target/scala-2.12/bc1-to-bc2-app-assembly-1.0.0.jar 

