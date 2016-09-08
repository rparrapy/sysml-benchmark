package config.fixtures

import com.typesafe.config.ConfigFactory
import org.peelframework.core.beans.data.{CopiedDataSet, DataSet, ExperimentOutput, GeneratedDataSet}
import org.peelframework.core.beans.experiment.ExperimentSequence.SimpleParameters
import org.peelframework.core.beans.experiment.{ExperimentSequence, ExperimentSuite}
import org.peelframework.flink.beans.experiment.FlinkExperiment
import org.peelframework.flink.beans.job.FlinkJob
import org.peelframework.flink.beans.system.Flink
import org.peelframework.hadoop.beans.experiment.YarnExperiment
import org.peelframework.hadoop.beans.system.{HDFS2, Yarn}
import org.peelframework.spark.beans.experiment.SparkExperiment
import org.peelframework.spark.beans.system.Spark
import org.peelframework.spark.beans.job.SparkJob
import org.springframework.context.{ApplicationContext, ApplicationContextAware}
import org.springframework.context.annotation.{Bean, Configuration}
import eu.stratosphere.benchmarks.systemml.yarn.YarnJob

/** `LinRegDS` experiment fixtures for the 'sysml-benchmark' bundle. */
@Configuration
class datasets extends ApplicationContextAware {
  /* The enclosing application context. */
  var ctx: ApplicationContext = null

  def setApplicationContext(ctx: ApplicationContext): Unit = {
    this.ctx = ctx
  }


  // ---------------------------------------------------
  // Data Generators
  // ---------------------------------------------------
  @Bean(name = Array("linreg.datagen.features"))
  def `datagen.linreg.features`: YarnJob = new YarnJob(
    command =
      s"""
         |jar $${app.path.apps}/SystemML.jar \\
	 |org.apache.sysml.api.DMLScript \\
         |-f $${app.path.apps}/scripts/datagen/genLinearRegressionData.dml \\
         |-nvargs numSamples=10000000 numFeatures=1000 maxFeatureValue=5 maxWeight=5 \\
         |addNoise=FALSE b=0 sparsity=1.0 output=$${system.hadoop-2.path.output}/linRegData.csv format=csv perc=0.5
        """.stripMargin.trim,
    runner  = ctx.getBean("yarn-2.7.1", classOf[Yarn]),
    timeout = 6000
  )

  // ---------------------------------------------------
  // Data Sets
  // ---------------------------------------------------
  @Bean(name = Array("linreg.dataset.features"))
  def `linreg.output.features`: DataSet = new GeneratedDataSet(
    src = ctx.getBean("linreg.datagen.features", classOf[YarnJob]),
    dst = "${system.hadoop-2.path.output}/linRegData.csv",
    fs  = ctx.getBean("hdfs-2.7.1", classOf[HDFS2])
  )
}
