#!/bin/bash

CLASSPATH=./target/rbm-0.0.1-SNAPSHOT.jar:/home/heikki/.m2/repository/org/encog/encog-core/3.3.0/encog-core-3.3.0.jar:../UByteReader/target/UByteReader-0.0.1-SNAPSHOT.jar 

JAVA_OPTS=-Dosgi.requiredJavaVersion\=1.8\ -XX:+UseG1GC\ -XX:+UseStringDeduplication\ -Dosgi.requiredJavaVersion\=1.8\ -Xms256m\ -Xmx1024m\ -verbose:classic

mvn clean package

java $JAVA_OPTS -classpath $CLASSPATH com.haem.ml.rbm.BoltzmannDemo ../UByteReader/resources/train-images.idx3-ubyte ../UByteReader/resources/train-labels.idx1-ubyte pre.eg raw.eg 
