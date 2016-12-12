package eu.stratosphere.benchmarks.systemml.cli.command.visualizationUtil

import scala.collection.mutable.ArrayBuffer

/**
 * Created by felix on 10.10.16.
 */
class TimestampData(epochc: Double, metricsValuesc: ArrayBuffer[Double]) {
  var epoch: Double = epochc
  var metricsValues: ArrayBuffer[Double] = metricsValuesc

  //TODO: don't sum up epochs (pass epoch index)
  def +=(timestampData: TimestampData) : TimestampData.this.type = {
    for (i <- 0 until metricsValues.size) {
      metricsValues(i) += timestampData.metricsValues(i)
    }
    this
  }
}
