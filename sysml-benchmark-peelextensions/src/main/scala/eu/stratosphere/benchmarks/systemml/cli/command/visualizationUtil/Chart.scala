package eu.stratosphere.benchmarks.systemml.cli.command.visualizationUtil

import java.io.File

import scalax.chart.api._
import scalax.chart.XYChart

/**
 * Created by felix on 11.10.16.
 */
class Chart(metricId: Int, chart: XYChart, folder: String, file: String) {
  val metricID = metricId
  var maxRuntime: Int = -1
  var maxValue: Double = -1
  
  def setMax(maxRuntimeP: Int, maxValueP: Double) = {
    maxRuntime = maxRuntimeP
    maxValue = maxValueP
  }
  
  def plotAndSave(isComparable: Boolean) {
    /*
    var legend = new LegendItemCollection
    val legenditem1 = new LegendItem("data-item")
    legend.add(legenditem1)
    chartSum.plot.setFixedLegendItems(legend)
    */
    
    val yAxis = chart.plot.getRangeAxis()
    if (isComparable) {
      if (maxValue > 0) {
        yAxis.setRange(0, maxValue)
      }
    }
    //yAxis.setLabel("test")
    

    val xAxis = chart.plot.getDomainAxis()
    if (isComparable) {
      xAxis.setRange(0, maxRuntime)
    }
    xAxis.setLabel("run time in seconds")

    new File(folder).mkdirs
    chart.saveAsPNG(folder + "/" + file)
  }
}
