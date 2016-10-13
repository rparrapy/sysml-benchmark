package eu.stratosphere.benchmarks.systemml.cli.command.visualizationUtil


import org.peelframework.core.beans.experiment.Experiment

import scala.collection.mutable.ArrayBuffer
import org.peelframework.core.beans.system.System

/**
 * Created by felix on 10.10.16.
 */
class ExperimentData(expPointer: Experiment[System], runsc: ArrayBuffer[RunData]) {
  var exp: Experiment[System] = expPointer
  var runs: ArrayBuffer[RunData] = runsc

  var maxValues: ArrayBuffer[Double] = null
  
  var summedRuns : RunData = new RunData()

  def this() {
    this(null, scala.collection.mutable.ArrayBuffer.empty[RunData])
  }
  def this(expPointer: Experiment[System]) {
    this(expPointer, scala.collection.mutable.ArrayBuffer.empty[RunData])
  }
  
  def insertNewTimeStepForLastInsertedRun(timestampData: TimestampData) {
    runs.last.insertNewTimeStepForLastInsertedNode(timestampData)

    //TODO: sum over all runs
  }

  def getMaxRuntime(): Int = {
    var runtimeMax: Int = -1
    for (r <- runs) {
      if (runtimeMax < r.getMaxRuntime()) {
        runtimeMax = r.getMaxRuntime()
      }
    }
    runtimeMax
  }
  
}
