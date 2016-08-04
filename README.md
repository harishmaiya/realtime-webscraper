##Summary:
---------
This application inputs Amazon product URLs and processes for interesting words in product description.

##Application stack:
-------------------
  Webserver: NodeJS
  
  Queue: RabbitMQ hosted on cloudamqp
  https://api.cloudamqp.com/console/24393069-4932-4e5d-8f6d-e002fe0e3af5/details
  
  Backend stream process engine: Flink & Scala
  
  DataStore: Not supported

##Steps to run:
--------------

###1. start Web server
  cd web-server
  
  node server.js


###2. start Backend server
  cd stream-process
  
  java -jar build/libs/stream-process-all.jar

###Build:
 gradle clean shadowJar

Webserver and Backend streaming engine communicate through RabbitMQ queue hosted on cloud.

