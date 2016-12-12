package eu.stratosphere.benchmarks.systemml.cli.command.visualizationUtil

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * Created by felix on 10.10.16.
 */
class NodeData(namec: String, timestepsc: mutable.HashMap[Long,TimestampData]) {
  var name: String = namec
  var timesteps: mutable.HashMap[Long,TimestampData] = timestepsc

  def this() {
    this("", new mutable.HashMap[Long,TimestampData]())
  }
  def this(namec: String) {
    this(namec, new mutable.HashMap[Long,TimestampData]())
  }

  def insertNewTimeStep(timestampData: TimestampData) = {
    timesteps.put(timestampData.epoch.toLong, timestampData)
  }
  
  def addToTimestamp(timestampData: TimestampData) = {
    val timestep = timesteps.getOrElse(timestampData.epoch.toLong, null)
    if (timestep != null) {
      timestep += timestampData 
      timesteps.put(timestampData.epoch.toLong, timestep)
    } else {
      timesteps.put(timestampData.epoch.toLong, timestampData)
    }
  }
  
  def getMetricTimeseries(metricID: Int): Array[Double] = {
    var values: ArrayBuffer[Double] = ArrayBuffer.empty[Double]
    for (t <- timesteps.toSeq.sortBy(t => t._1)) {
      values += t._2.metricsValues(metricID)
    }
    values.toArray[Double]
  }
}
