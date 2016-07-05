package conf.fixtures

import com.typesafe.config.ConfigFactory
import org.peelframework.core.beans.data.{CopiedDataSet, DataSet, ExperimentOutput, GeneratedDataSet}
import org.peelframework.core.beans.experiment.ExperimentSequence.SimpleParameters
import org.peelframework.core.beans.experiment.{ExperimentSequence, ExperimentSuite}
import org.peelframework.flink.beans.experiment.FlinkExperiment
import org.peelframework.flink.beans.job.FlinkJob
import org.peelframework.flink.beans.system.Flink
import org.peelframework.hadoop.beans.experiment.YarnExperiment
import org.peelframework.hadoop.beans.system.HDFS2
import org.peelframework.spark.beans.experiment.SparkExperiment
import org.peelframework.spark.beans.system.Spark
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.{Bean, Configuration}

/** `LinRegDS` experiment fixtures for the 'sysml-benchmark' bundle. */
@Configuration
class linregDS extends ApplicationContextAware {
  /* The enclosing application context. */
  var ctx: ApplicationContext = null

  def setApplicationContext(ctx: ApplicationContext): Unit = {
    this.ctx = ctx
  }

  // ---------------------------------------------------
  // Data Sets
  // ---------------------------------------------------

  @Bean(name = Array("dataset.features.generated"))
  def `dataset.features.generated`: DataSet = new GeneratedDataSet(
    src = ctx.getBean("datagen.features", classOf[YarnExperiment]),
    dst = "${system.hadoop-2.path.input}/features.mat",
    fs  = ctx.getBean("hdfs-2.7.1", classOf[HDFS2])
  )

  @Bean(name = Array("linreg.output.features"))
  def `linreg.output`: ExperimentOutput = new ExperimentOutput(
    path = "${system.hadoop-2.path.output}/linreg/features",
    fs  = ctx.getBean("hdfs-2.7.1", classOf[HDFS2])
  )

  // ---------------------------------------------------
  // Experiments
  // ---------------------------------------------------

  @Bean(name = Array("linreg.data.generate"))
  def `linreg.data.generate`: ExperimentSuite = {
    val `linreg.data.generate.features` = new YarnExperiment(
      name    = "linreg.data.generate.features",
      command =
        s"""
          | jar ${app.path.apps}/SystemML.jar \
          | org.apache.sysml.api.DMLScript -f ${app.path.apps}/scripts/datagen/genLinearRegressionData.dml \
          | -nvargs numSamples=1000 numFeatures=50 maxFeatureValue=5 maxWeight=5 \
          | addNoise=FALSE b=0 sparsity=0.7 output=linRegData.csv format=csv perc=0.5
        """.stripMargin.trim,
      config  = ConfigFactory.parseString(""),
      runs    = 1,
      runner  = ctx.getBean("yarn-2.7.1", classOf[Yarn]),
      inputs  = Set(),
      outputs = Set(ctx.getBean("linreg.output.features", classOf[ExperimentOutput]))
    )

    new ExperimentSuite(Seq(
      `linreg.data.generate.features`
    ))
  }
}