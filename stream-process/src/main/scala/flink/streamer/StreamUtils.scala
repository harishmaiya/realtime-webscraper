package flink.streamer

import java.lang.Boolean
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentHashMap.KeySetView

import org.apache.flink.api.common.functions.MapFunction
import org.apache.flink.streaming.api.checkpoint.Checkpointed

import scala.collection.mutable.ListBuffer

/**
  * @author harish.maiya
  */
package object StreamUtils {

  val CHECKPOINT_INTERVAL = 5000
  //in milli seconds
  var ProdIDStore: KeySetView[String, Boolean] = ConcurrentHashMap.newKeySet()
  var WordCount = new ListBuffer[Tuple2[String, Int]]()
  val stopWords = Set("a", "the", "to", "for", "of", "from", "at", "it", "and")

  case class ProductData(prodID: String, prodURL: String)

  //NOTES:
  //This Class and methods are needed to support checkpoints - store Flink state variables across sessions and failures
  //Currently using MemoryStateBacked for checkpointing IDStore where Default size is 5MB. Need FsStateBackend (Filesystem) for production quality
  //For Production quality: Need Stateful Source Functions with support of locking
  class MapWithProdID extends MapFunction[String, KeySetView[String, Boolean]] with
    Checkpointed[KeySetView[String, Boolean]] {

    //For Production quality: Use memcached or other persistence solutions
    def map(prodID: String): KeySetView[String, Boolean] = {
      ProdIDStore.add(prodID)
      ProdIDStore
    }

    def snapshotState(cpId: Long, cpTimestamp: Long): KeySetView[String, Boolean] = {
      ProdIDStore
    }

    def restoreState(state: KeySetView[String, Boolean]) {
      ProdIDStore = state
    }
  }

  class MapWithWordCount extends MapFunction[Tuple2[String, Int], Map[String, Int]] with
    Checkpointed[ListBuffer[Tuple2[String, Int]]] {
    //For Production quality: Use memcached or other persistence solutions

    def map(currWord: Tuple2[String, Int]): Map[String, Int] = {
      WordCount += currWord
      WordCount
        .groupBy(_._1)
        .map {
          case (word, count) => (word, count.map(_._2).sum)
        }
    }

    def snapshotState(cpId: Long, cpTimestamp: Long): ListBuffer[(String, Int)] = {
      WordCount
    }

    def restoreState(state: ListBuffer[Tuple2[String, Int]]) {
      WordCount = state
    }
  }

}
