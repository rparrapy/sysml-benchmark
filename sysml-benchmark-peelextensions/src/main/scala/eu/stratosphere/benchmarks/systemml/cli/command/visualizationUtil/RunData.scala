package eu.stratosphere.benchmarks.systemml.cli.command.visualizationUtil

import scala.collection.mutable.ArrayBuffer

/**
 * Created by felix on 10.10.16.
 */
class RunData(idc: Int, nodesc: ArrayBuffer[NodeData]) {
  var id: Int = idc 
  var nodes: ArrayBuffer[NodeData] = nodesc
  
  var maxValues: ArrayBuffer[Double] = null
  
  var clusterAggregatedSum : NodeData = new NodeData("cluster")
  
  def this() {
    this(0, scala.collection.mutable.ArrayBuffer.empty[NodeData])
  }
  def this(idc: Int) {
    this(idc, scala.collection.mutable.ArrayBuffer.empty[NodeData])
  }

  def insertNewTimeStepForLastInsertedNode(timestampData: TimestampData) {
    nodes.last.insertNewTimeStep(timestampData)
    
    //aggregated sum
    clusterAggregatedSum.addToTimestamp(timestampData)
  }
  
  def getMaxRuntime(): Int = {
    var runtimeMax: Int = -1
    for (n <- nodes) {
      if (runtimeMax < n.timesteps.size) {
        runtimeMax = n.timesteps.size
      }
    }
    runtimeMax
  }
}
