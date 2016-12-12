package eu.stratosphere.benchmarks.systemml.cli.command.visualizationUtil

import org.peelframework.core.beans.experiment.ExperimentSuite

import scala.collection.mutable.ArrayBuffer

class SuiteData(suitePointer: ExperimentSuite, schemac : Schema, experimentsc: ArrayBuffer[ExperimentData]) {
  var suite: ExperimentSuite = suitePointer
  var dstatMetricsSchema : Schema = schemac
  var experiments: ArrayBuffer[ExperimentData] = experimentsc

  var maxValues: ArrayBuffer[Double] = null

  def this() {
    this(null, null, scala.collection.mutable.ArrayBuffer.empty[ExperimentData])
  }
  def this(suitePointer: ExperimentSuite) {
    this(suitePointer, null, scala.collection.mutable.ArrayBuffer.empty[ExperimentData])
  }
  
  def insertNewTimeStepForLastInsertedExperiment(values: ArrayBuffer[Double]) {
    val timestamp = new TimestampData(values(dstatMetricsSchema.epochID), values)
    experiments.last.insertNewTimeStepForLastInsertedRun(timestamp)
    
  }

  def getMaxRuntime(): Int = {
    var runtimeMax: Int = -1
    for (e <- experiments) {
      if (runtimeMax < e.getMaxRuntime()) {
        runtimeMax = e.getMaxRuntime()
      }
    }
    runtimeMax
  }
}
