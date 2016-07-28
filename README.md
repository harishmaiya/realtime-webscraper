STEPS to RUN

Webserver - NodeJs:
--------------------
cd web-server
node server.js

Backend server - Flink/Scala:
------------------------------
cd stream-process
java -jar build/libs/stream-process-all.jar

To build:
gradle clean shadowJar

Webserver and Backend streaming engine talk communicate through RabbitMQ queue hosted on cloud.
https://api.cloudamqp.com/console/24393069-4932-4e5d-8f6d-e002fe0e3af5/details

