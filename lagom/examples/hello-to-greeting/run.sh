#! /bin/bash
LAUNCHER_DIR=$(dirname $0)
HOSTNAME=192.168.0.78
AKKAPORT=2553
HTTPPORT=9000
SEED_NODE="$HOSTNAME:$AKKAPORT"
APPLICATION_SECRET="cfd16c3a-f0f2-4fa9-8e58-ff9a2ad2a422"

java -DHOSTNAME=$HOSTNAME \
	-DPORT=$AKKAPORT \
	-DSEED_NODE=$SEED_NODE \
	-DAPPLICATION_SECRET=$APPLICATION_SECRET \
	-Dplay.server.http.port=$HTTPPORT \
    -jar $LAUNCHER_DIR/hello-to-greeting-app/target/scala-2.12/hello-to-greeting-app-assembly-1.0.0.jar \
    #&>> hello-to-greeting-app.log &

