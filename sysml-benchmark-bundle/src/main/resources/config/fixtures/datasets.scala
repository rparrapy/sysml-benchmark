package config.fixtures

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
import org.springframework.context.{ApplicationContext, ApplicationContextAware}
import org.springframework.context.annotation.{Bean, Configuration}

/** `LinRegDS` experiment fixtures for the 'sysml-benchmark' bundle. */
@Configuration
class datasets extends ApplicationContextAware {
  /* The enclosing application context. */
  var ctx: ApplicationContext = null

  def setApplicationContext(ctx: ApplicationContext): Unit = {
    this.ctx = ctx
  }

  // ---------------------------------------------------
  // Data Sets
  // ---------------------------------------------------
  @Bean(name = Array("linreg.output.features"))
  def `linreg.output`: ExperimentOutput = new ExperimentOutput(
    path = "${system.hadoop-2.path.output}/linreg/features",
    fs  = ctx.getBean("hdfs-2.7.1", classOf[HDFS2])
  )
}