package flink.streamer

import java.net.URL

import flink.streamer.StreamUtils._
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import play.api.libs.json.Json

/**
  * @author harish.maiya
  */
object FlinkStream {

  val cloudRMQ = "amqp://wbeczfqc:DG86frxWObe6OTz8-iGl0YAyhaRZtpkr@hyena.rmq.cloudamqp.com/wbeczfqc"
  val queueName = "AMZNProds"

  def main(args: Array[String]) {

    val prodIDMap = new MapWithProdID()
    val wordCount = new MapWithWordCount()

    val streamEnv = StreamExecutionEnvironment.getExecutionEnvironment
    val amznStream = streamEnv.addSource(new CustomRMQSource[String](cloudRMQ, queueName, true, new SimpleStringSchema()))

    val uniqueProdStream = amznStream
                           .filter(_.nonEmpty)
                           .map {
                             prodInput =>
                               val prodJSON = Json.parse(prodInput)
                               ProductData((prodJSON \ "productID").validate[String].get, (prodJSON \ "url").validate[String].get)
                           }
                           .filter { prodStream =>
                             !ProdIDStore.contains(prodStream.prodID)
                           }

    val wordStream = uniqueProdStream
                     .map {
                       prodStream =>
                         println("Streaming received ProdURL: " + prodStream.prodURL)
                         prodIDMap.map(prodStream.prodID) //Put received unique ProdID to KeyStore
                         CustomScraper.productDescription(new URL(prodStream.prodURL)) //Do scraping
                     }
                     .map {
                       _.get.get.description.toLowerCase
                       .replaceAll(""""[^a-zA-Z]""", " ")
                     }
                     .flatMap {
                       _.split("\\W+")
                     }
                     .filter(!stopWords.contains(_))
                     .map {
                       (_, 1)
                     }
                     .map(wordCount)
    //Can provide time window for processing wordcount, if required

    wordStream.print()

    streamEnv.execute()
  }
}