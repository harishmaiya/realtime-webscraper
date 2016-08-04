##Summary:
---------
Application inputs Amazon product URLs and process for interesting words in product description. Outputs list of words by their popularity.

##Application stack:
-------------------
  Webserver: NodeJS
  
  Queue: RabbitMQ hosted on cloudamqp
  https://api.cloudamqp.com/console/24393069-4932-4e5d-8f6d-e002fe0e3af5/details
  
  Backend stream process engine: Flink & Scala
  
  DataStore: Not supported

##Start services:
--------------

###1. Web server
  cd web-server
  
  node server.js
  Application uses port: 9200


###2. Backend/Stream server
  cd stream-process
  
  java -jar build/libs/stream-process-all.jar

##Run:
Intitialize webserver through GET: 

http://localhost:9200/api/start

Subsequent POST calls to include Amazon Products:

http://localhost:9200/api/url?
Body: http://www.amazon.com/gp/product/B00VVOCSOU


###Build:
 gradle clean shadowJar


Webserver and Backend streaming engine communicate through RabbitMQ queue hosted on cloud.

