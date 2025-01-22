#!/bin/sh

# block until kafka is reachable
kafka-topics --bootstrap-server kafka1:9092 --list

echo -e 'Deleting kafka topics'
kafka-topics --bootstrap-server kafka1:9092 --topic meeting-registration-request --delete --if-exists
kafka-topics --bootstrap-server kafka1:9092 --topic meeting-approval-request --delete --if-exists
kafka-topics --bootstrap-server kafka1:9092 --topic notification-request --delete --if-exists
kafka-topics --bootstrap-server kafka1:9092 --topic notification-request-dlt --delete --if-exists

echo -e 'Creating kafka topics'
kafka-topics --bootstrap-server kafka1:9092 --create --if-not-exists --topic meeting-registration-request --replication-factor 3 --partitions 3
kafka-topics --bootstrap-server kafka1:9092 --create --if-not-exists --topic meeting-approval-request --replication-factor 3 --partitions 3
kafka-topics --bootstrap-server kafka1:9092 --create --if-not-exists --topic notification-request --replication-factor 3 --partitions 3
kafka-topics --bootstrap-server kafka1:9092 --create --if-not-exists --topic notification-request-dlt --replication-factor 3 --partitions 3


echo -e 'Successfully created the following topics:'
kafka-topics --bootstrap-server kafka1:9092 --list
