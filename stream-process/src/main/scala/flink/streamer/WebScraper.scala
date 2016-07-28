package flink.streamer

import java.net.URL

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.scraper.ContentExtractors._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import util.control.Breaks._

import scala.io.Source
import scala.util.Try

/**
  * @author harish.maiya
  */
trait WebScraper {
  case class ProductDescription(description: String)
  protected def productDescription(url: URL): Try[Option[ProductDescription]]
}

//Regex solution
object CustomScraper
  extends WebScraper {
  private val startEx = "<div class=\"productDescriptionWrapper\">".r
  private val endEx = "<div class=\"emptyClear\">".r
  private var isProdDesc = false
  private var productDesc: Option[ProductDescription] = None

  val strbuilder = StringBuilder.newBuilder

  override def productDescription(url: URL): Try[Option[ProductDescription]] = Try {
    for (line <- Source.fromURL(url).getLines) {  //Trying same Amzn URL consecutive times seems to cause http 503 error
    //for (line <- Source.fromFile("/Users/harish.maiya/Code/exp/amazn.txt").getLines) {  //File from wget of URL
      breakable {
          if (startEx.findAllIn(line).nonEmpty) {
            isProdDesc = true
            break
          }
          if (endEx.findAllIn(line).nonEmpty && isProdDesc) {
            isProdDesc = false
          }
          if (isProdDesc) {
            strbuilder.append(line)
          }
        }
    }
    println("Raw description scraped:\n" + strbuilder.toString())
    Some(ProductDescription(strbuilder.toString()))
  }
}

//Using scalascraper library
object LibScraper
  extends WebScraper {
  lazy val browser = new JsoupBrowser()

  override def productDescription(url: URL): Try[Option[ProductDescription]] = Try {

    val urlDoc = browser.get(url.toString)

    val prodDesc = urlDoc >?> element("#productDescription p").map {
      elem =>
        ProductDescription(elem.text)
    }

    prodDesc
  }
}
