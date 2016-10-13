package eu.stratosphere.benchmarks.systemml.cli.command.visualizationUtil

import scala.collection.mutable.HashMap
import scala.collection.mutable.ArrayBuffer

/**
 * Created by felix on 10.10.16.
 */
class Schema(listc: ArrayBuffer[Metric], mapc: HashMap[String, Int]) {

  var list: ArrayBuffer[Metric] = listc
  var epochID = -1

  def this (listt: ArrayBuffer[Metric]) {
    this(listt, new HashMap[String, Int]())
    
    for (m <- list) {
      if (m.isEpoch()) {
        epochID = m.id
      }
    }
  }
  
  def size() : Int = {
    list.size
  }
}
