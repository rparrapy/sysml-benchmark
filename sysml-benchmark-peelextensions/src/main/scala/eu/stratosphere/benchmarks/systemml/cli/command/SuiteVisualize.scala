/**
 * Copyright (C) 2014 TU Berlin (peel@dima.tu-berlin.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.stratosphere.benchmarks.systemml.cli.command

import java.lang.{System => Sys}
import java.nio.file.Paths

import net.sourceforge.argparse4j.inf.{Namespace, Subparser}
import org.peelframework.core.beans.experiment.{ExperimentSuite}
import org.peelframework.core.cli.command.Command
import org.peelframework.core.config.{loadConfig}
import org.peelframework.core.graph.createGraph
import org.peelframework.core.util.console._
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import java.io.{File, FilenameFilter}

import org.jfree.chart.{LegendItem, LegendItemCollection}
import java.awt.Color

import eu.stratosphere.benchmarks.systemml.cli.command.visualizationUtil._
import org.peelframework.core.beans.experiment.Experiment.RunName

import scala.collection.mutable.ArrayBuffer
import scalax.chart.api._

import net.sourceforge.argparse4j.impl.Arguments

/** Execute all experiments in a suite. */
@Service("suite:vi")
class SuiteVisualize extends Command {

  override val help = "visualize all experiments in a suite"

  override def register(parser: Subparser) = {
    
    // options
    parser.addArgument("--compare", "-c")
        .`type`(classOf[Boolean])
        .dest("app.suite.experiment.compare")
        .action(Arguments.storeTrue)
        .help("generate charts which are comparable between experiments")
    // arguments
    parser.addArgument("suite")
        .`type`(classOf[String])
        .dest("app.suite.name")
        .metavar("SUITE")
        .help("experiments suite to run")
  }

  override def configure(ns: Namespace) = {
    // set ns options and arguments to system properties
    Sys.setProperty("app.suite.experiment.compare", if (ns.getBoolean("app.suite.experiment.compare")) "true" else "false")
    Sys.setProperty("app.suite.name", ns.getString("app.suite.name"))
  }

  override def run(context: ApplicationContext) = {
    val compare = Sys.getProperty("app.suite.experiment.compare", "false") == "true"
    val suiteName = Sys.getProperty("app.suite.name")

    logger.info(s"Running experiments in suite '$suiteName'")

    val suite = context.getBean(suiteName, classOf[ExperimentSuite])
    val graph = createGraph(suite)

    //TODO check for cycles in the graph
    if (graph.isEmpty)
      throw new RuntimeException("Experiment suite is empty!")

    // load application configuration
    implicit val config = loadConfig()

    // resolve paths
    val homePath = Paths.get(config.getString("app.path.home")).normalize.toAbsolutePath
    val resultsPath = Paths.get(config.getString("app.path.results")).normalize.toAbsolutePath
    val suitePath = Paths.get(resultsPath.toString, suite.name)
    
    val chartDir = Paths.get(homePath.toString, "charts")
    chartDir.toFile.mkdir()
    
    println("home: " + homePath)

    def getListOfSubDirectories(directoryName: String): Array[String] = {
      return (new File(directoryName)).listFiles.filter(_.isDirectory).map(_.getName)
    }

    def getListOfFiles(directoryName: String): Array[String] = {
      return (new File(directoryName)).listFiles(new FilenameFilter() {
        def accept(dir:File, name: String): Boolean = {
          name.toLowerCase().endsWith(".csv");
        }
      }).filter(_.isFile).map(_.getName)
    }

    // SUITE lifespan
    try {
      logger.info("Visualizing experiments in suite")
      for (e <- suite.experiments) {
        print ("experiment: " + e.name + " path: " + suitePath + "\n")
      }
      
      /*
      if (!new File(suitePath.toString).exists()){
        throw new Exception("no results found for suite " + suiteName)
      }*/

      val suitePathString = "/home/felix/broadcast/ibm-power-1-one-dir" //suitePath.toString

      var suiteData = new SuiteData(suite)

      var cats: ArrayBuffer[Category] = null
      var headers: ArrayBuffer[Metric] = null
      
      for (e <- suite.experiments) {
        println(e.name + " - " + e.runs)
        var experimentData = new ExperimentData(e)
        suiteData.experiments += experimentData
        for (r <- 1 to e.runs) {
          var runData = new RunData(r)
          val expDir: String = RunName(e.name, r)

          val dstatFolder = suitePathString + "/" + expDir + "/logs/dstat/dstat-0.7.2"
          
          if (new File(dstatFolder).exists()) {
            suiteData.experiments.last.runs += runData

            for (csv <- getListOfFiles(dstatFolder)) {
              println("csv: " + csv)

              var node = new NodeData(csv)
              suiteData.experiments.last.runs.last.nodes += node

              val bufferedSource = io.Source.fromFile(dstatFolder + "/" + csv)
              var i = 1
              for (line <- bufferedSource.getLines) {
                val cols = line.split(",").map(_.trim)
                //register categories
                if (cats == null && i == 6) {
                  cats = ArrayBuffer.empty[Category]
                  for (c <- 0 until cols.size) {
                    if (cols(c).length > 0) {
                      val name = cols(c).map(c => if (c == '/' || c == ' ') '_' else c)
                          .filterNot(_ == '"')
                      cats += new Category(name, c)
                    } else {
                      cats.last.endID = c
                    }
                  }
                }
                //register titles
                if (headers == null && i == 7) {
                  var headers = ArrayBuffer.empty[Metric]
                  val headersArray = cols.map(s => s.filterNot(_ == '"'))

                  for (h <- 0 until headersArray.size) {
                    for (c <- cats) {
                      if (c.startID <= h && c.endID >= h) {
                        headers += new Metric(h, headersArray(h), c)
                      }
                    }
                  }
                  suiteData.dstatMetricsSchema = new Schema(headers)
                }
                if (i > 7) {
                  suiteData.insertNewTimeStepForLastInsertedExperiment((cols.map(_.toDouble).to[ArrayBuffer]))
                }
                i += 1
              }
              bufferedSource.close
            }
          }
        }
      }

      //check for complete monitoring timespan
      for (exp <- suiteData.experiments) {
        for (run <- exp.runs) {
          var timestepsMax = -1
          for (node <- run.nodes) {
            if (node.timesteps.size > timestepsMax) {
              timestepsMax = node.timesteps.size
            }
          }
          
          for (node <- run.nodes) {
            if (timestepsMax != node.timesteps.size) {
              logger.warn("host: " + node.name + " timesteps: " + node.timesteps.size + " != " + timestepsMax)
            }
          }
        }
      }
      
      //calculate timeseries for charts
      var chartsAgg = ArrayBuffer.empty[Chart]      
      var maxValuesAgg: Array[Double] = Array.fill[Double](suiteData.dstatMetricsSchema.size)(-1)

      var chartsAggCum = ArrayBuffer.empty[Chart]
      var maxValuesAggCum: Array[Double] = Array.fill[Double](suiteData.dstatMetricsSchema.size)(-1)

      for (exp <- suiteData.experiments) {
        for (run <- exp.runs) {
          for (m <- 0 until suiteData.dstatMetricsSchema.size) {
            val metric = suiteData.dstatMetricsSchema.list(m)
            if (!metric.isEpoch) {
              var divisor = metric.category.divisor
              val timeseries = run.clusterAggregatedSum.getMetricTimeseries(m)
              val chartDataSum = for (i <- 1 to run.clusterAggregatedSum.timesteps.size) 
                yield (i, timeseries(i - 1) / divisor)
              
              val title = metric.category.name + " - " + metric.name
              val folder = chartDir + "/" + suiteName + "/" + exp.exp.name + "/sum/" + metric.category.name
              val file = "agg_chart_" + metric.category.name + "_" + metric.name + ".png"
              
              val currentMax = timeseries.max / divisor
              if (maxValuesAgg(m) < currentMax) {
                maxValuesAgg(m) = currentMax
              }
              
              val chartSum = XYLineChart(chartDataSum, title = title)
              chartsAgg += new Chart(m, chartSum, folder, file)
              
              //cumulative aggregate
              val timeSeriesCum = timeseries.clone()
              for (ts <- 1 until timeSeriesCum.size) {
                timeSeriesCum(ts) += timeSeriesCum(ts-1)
              }
              
              val chartDataCumSum = for (i <- 1 to run.clusterAggregatedSum.timesteps.size) 
                yield (i, timeSeriesCum(i - 1) / divisor)
              val chartCumSum = XYLineChart(chartDataCumSum, title = title)

              val folder1 = chartDir + "/" + suiteName + "/" + exp.exp.name + "/cumulative/" + metric.category.name
              val file1 = "cum_agg_chart_" + metric.category.name + "_" + metric.name + ".png"

              chartsAggCum += new Chart(m, chartCumSum, folder1, file1)

              val currentMaxCum = timeSeriesCum.max / divisor
              if (maxValuesAggCum(m) < currentMaxCum) {
                maxValuesAggCum(m) = currentMaxCum
              }
            }
          }
        }
      }
      
      //draw & save plots
      for (chart <- chartsAgg) {
        chart.setMax(suiteData.getMaxRuntime(), maxValuesAgg(chart.metricID))
        chart.plotAndSave(compare)
      }
      for (chart <- chartsAggCum) {
        chart.setMax(suiteData.getMaxRuntime(), maxValuesAggCum(chart.metricID))
        chart.plotAndSave(compare)
      }
      
    }
    catch {
      case e: Throwable =>
        logger.error(s"Exception in suite '${suite.name}': ${e.getMessage}".red)
        throw e

    } finally {
      // Explicit shutdown only makes sense if at least one experiment is in the list,
      // otherwise the systems would not have been configured at all
      
    }
  }
}
