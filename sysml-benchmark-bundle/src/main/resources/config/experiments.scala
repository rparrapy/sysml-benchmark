package config

import com.typesafe.config.ConfigFactory
import org.peelframework.core.beans.data.{CopiedDataSet, DataSet, ExperimentOutput, GeneratedDataSet}
import org.peelframework.core.beans.experiment.ExperimentSuite
import org.peelframework.hadoop.beans.experiment.YarnExperiment
import org.peelframework.spark.beans.experiment.SparkExperiment
import org.peelframework.spark.beans.system.Spark
import org.peelframework.flink.beans.system.Flink
import eu.stratosphere.benchmarks.systemml.flink.FlinkExperimentSysML
import org.peelframework.hadoop.beans.system.{Yarn, HDFS2}
import org.springframework.context.annotation._
import org.springframework.context.{ApplicationContext, ApplicationContextAware}

/** Experiments definitions for the 'sysml-benchmark' bundle. */
@Configuration
@ComponentScan( // Scan for annotated Peel components in the 'eu.stratosphere.benchmarks.systemml' package
  value = Array("eu.stratosphere.benchmarks.systemml"),
  useDefaultFilters = false,
  includeFilters = Array[ComponentScan.Filter](
    new ComponentScan.Filter(value = Array(classOf[org.springframework.stereotype.Service])),
    new ComponentScan.Filter(value = Array(classOf[org.springframework.stereotype.Component]))
  )
)
@ImportResource(value = Array(
  "classpath:peel-core.xml",
  "classpath:peel-extensions.xml"
))
@Import(value = Array(
  classOf[org.peelframework.extensions], // custom system beans
  classOf[config.fixtures.systems],      // custom system beans
  classOf[config.fixtures.datasets]
))
class experiments extends ApplicationContextAware {

  val runs = 1
  
  //don't format hdfs after experiment / suite to keep the data set
  val no_format = ConfigFactory.parseString(
    s"""
       |system.hadoop-2 {
       |  format = false
       |}
         """.stripMargin)

  /* The enclosing application context. */
  var ctx: ApplicationContext = null

  def setApplicationContext(ctx: ApplicationContext): Unit = {
    this.ctx = ctx
  }

  // ---------------------------------------------------
  // Experiments
  // Dev mode
  // generating the dataset only once
  // ---------------------------------------------------

  @Bean(name = Array("linregDS.train.ds.provided"))
  def `linregDS.train.ds.provided`: ExperimentSuite = {
    val `linreg.train.spark` = new SparkExperiment(
      name    = "linreg.train.spark",
      command =
        s"""
           |--class org.apache.sysml.api.DMLScript \\
           |$${app.path.apps}/SystemML.jar \\
           |-f $${app.path.apps}/scripts/algorithms/LinearRegDS.dml -exec hybrid_spark -explain -nvargs \\
           |X=$${system.hadoop-2.path.output}/linRegData.train.data.bin \\
           |Y=$${system.hadoop-2.path.output}/linRegData.train.labels.bin \\
           |B=$${system.hadoop-2.path.output}/betas.csv fmt=csv
         """.stripMargin.trim,
      config = no_format,
      runs   = runs,
      runner = ctx.getBean("spark-1.6.0-provided", classOf[Spark]),
      inputs = Set(ctx.getBean("linreg.dataset.features.provided", classOf[DataSet]), ctx.getBean("linreg.dataset.features_split.provided", classOf[DataSet])),
      outputs = Set(ctx.getBean("linreg.train.ds.output.provided", classOf[ExperimentOutput]))
    )

    val `linreg.train.flink` = new FlinkExperimentSysML(
      name    = "linreg.train.flink",
      command =
        s"""
           |-c org.apache.sysml.api.DMLScript \\
           |-C file://$${app.path.apps}/hadoop-mapreduce-client-jobclient-2.7.1.jar $${app.path.apps}/SystemML.jar \\
           |-f $${app.path.apps}/scripts/algorithms/LinearRegDS.dml -exec hybrid_flink -explain -nvargs \\
           |X=$${system.hadoop-2.path.output}/linRegData.train.data.bin \\
           |Y=$${system.hadoop-2.path.output}/linRegData.train.labels.bin \\
           |B=$${system.hadoop-2.path.output}/betas.csv fmt=csv
         """.stripMargin.trim,
      config = no_format,
      runs   = runs,
      runner = ctx.getBean("flink-1.0.3-provided", classOf[Flink]),
      inputs = Set(ctx.getBean("linreg.dataset.features.provided", classOf[DataSet]), ctx.getBean("linreg.dataset.features_split.provided", classOf[DataSet])),
      outputs = Set(ctx.getBean("linreg.train.ds.output.provided", classOf[ExperimentOutput]))
    )

    val `linreg.train.yarn` = new YarnExperiment(
      name    = "linreg.train.yarn",
      command =
        s"""
           |$${app.path.apps}/SystemML.jar \\
           |org.apache.sysml.api.DMLScript \\
           |-f $${app.path.apps}/scripts/algorithms/LinearRegDS.dml -explain -nvargs \\
           |X=$${system.hadoop-2.path.output}/linRegData.train.data.bin \\
           |Y=$${system.hadoop-2.path.output}/linRegData.train.labels.bin \\
           |B=$${system.hadoop-2.path.output}/betas.csv fmt=csv
         """.stripMargin.trim,
      config = no_format,
      runs   = runs,
      runner = ctx.getBean("yarn-2.7.1-provided", classOf[Yarn]),
      inputs = Set(ctx.getBean("linreg.dataset.features.provided", classOf[DataSet]), ctx.getBean("linreg.dataset.features_split.provided", classOf[DataSet])),
      outputs = Set(ctx.getBean("linreg.train.ds.output.provided", classOf[ExperimentOutput])),
      systems = Set()
    )

    new ExperimentSuite(Seq(
      `linreg.train.spark`,
      `linreg.train.flink`,
      `linreg.train.yarn`
    ))
  }

  @Bean(name = Array("linregCG.train.ds.provided"))
  def `linregCG.train.ds.provided`: ExperimentSuite = {
    val `linreg.train.spark` = new SparkExperiment(
      name    = "linreg.train.spark",
      command =
        s"""
           |--class org.apache.sysml.api.DMLScript \\
           |$${app.path.apps}/SystemML.jar \\
           |-f $${app.path.apps}/scripts/algorithms/LinearRegCG.dml -exec hybrid_spark -explain -nvargs \\
           |X=$${system.hadoop-2.path.output}/linRegData.train.data.bin \\
           |Y=$${system.hadoop-2.path.output}/linRegData.train.labels.bin \\
           |B=$${system.hadoop-2.path.output}/betas.csv maxi=6 fmt=csv
         """.stripMargin.trim,
      config = no_format,
      runs   = runs,
      runner = ctx.getBean("spark-1.6.0-provided", classOf[Spark]),
      inputs = Set(ctx.getBean("linreg.dataset.features.provided", classOf[DataSet]), ctx.getBean("linreg.dataset.features_split.provided", classOf[DataSet])),
      outputs = Set(ctx.getBean("linreg.train.ds.output.provided", classOf[ExperimentOutput]))
    )

    val `linreg.train.flink` = new FlinkExperimentSysML(
      name    = "linreg.train.flink",
      command =
        s"""
           |-c org.apache.sysml.api.DMLScript \\
           |-C file://$${app.path.apps}/hadoop-mapreduce-client-jobclient-2.7.1.jar $${app.path.apps}/SystemML.jar \\
           |-f $${app.path.apps}/scripts/algorithms/LinearRegCG.dml -exec hybrid_flink -explain -nvargs \\
           |X=$${system.hadoop-2.path.output}/linRegData.train.data.bin \\
           |Y=$${system.hadoop-2.path.output}/linRegData.train.labels.bin \\
           |B=$${system.hadoop-2.path.output}/betas.csv maxi=6 fmt=csv
         """.stripMargin.trim,
      config = no_format,
      runs   = runs,
      runner = ctx.getBean("flink-1.0.3-provided", classOf[Flink]),
      inputs = Set(ctx.getBean("linreg.dataset.features.provided", classOf[DataSet]), ctx.getBean("linreg.dataset.features_split.provided", classOf[DataSet])),
      outputs = Set(ctx.getBean("linreg.train.ds.output.provided", classOf[ExperimentOutput]))
    )

    val `linreg.train.yarn` = new YarnExperiment(
      name    = "linreg.train.yarn",
      command =
        s"""
           |$${app.path.apps}/SystemML.jar \\
           |org.apache.sysml.api.DMLScript \\
           |-f $${app.path.apps}/scripts/algorithms/LinearRegCG.dml -explain -nvargs \\
           |X=$${system.hadoop-2.path.output}/linRegData.train.data.bin \\
           |Y=$${system.hadoop-2.path.output}/linRegData.train.labels.bin \\
           |B=$${system.hadoop-2.path.output}/betas.csv maxi=6 fmt=csv
         """.stripMargin.trim,
      config = no_format,
      runs   = runs,
      runner = ctx.getBean("yarn-2.7.1-provided", classOf[Yarn]),
      inputs = Set(ctx.getBean("linreg.dataset.features.provided", classOf[DataSet]), ctx.getBean("linreg.dataset.features_split.provided", classOf[DataSet])),
      outputs = Set(ctx.getBean("linreg.train.ds.output.provided", classOf[ExperimentOutput])),
      systems = Set()
    )

    new ExperimentSuite(Seq(
      `linreg.train.spark`,
      `linreg.train.flink`,
      `linreg.train.yarn`
    ))
  }

  // ---------------------------------------------------
  // Experiments
  // User mode
  // generating the dataset for every suite
  // ---------------------------------------------------

  @Bean(name = Array("linregDS.train.ds"))
  def `linregDS.train.ds`: ExperimentSuite = {
    val `linreg.train.spark` = new SparkExperiment(
      name    = "linreg.train.spark",
      command =
          s"""
             |--class org.apache.sysml.api.DMLScript \\
             |$${app.path.apps}/SystemML.jar \\
             |-f $${app.path.apps}/scripts/algorithms/LinearRegDS.dml -exec hybrid_spark -explain -nvargs \\
             |X=$${system.hadoop-2.path.output}/linRegData.train.data.bin \\
             |Y=$${system.hadoop-2.path.output}/linRegData.train.labels.bin \\
             |B=$${system.hadoop-2.path.output}/betas.csv fmt=csv
         """.stripMargin.trim,
      config = ConfigFactory.parseString(""),
      runs   = runs,
      runner = ctx.getBean("spark-1.6.0", classOf[Spark]),
      inputs = Set(ctx.getBean("linreg.dataset.features", classOf[DataSet]), ctx.getBean("linreg.dataset.features_split", classOf[DataSet])),
      outputs = Set(ctx.getBean("linreg.train.ds.output", classOf[ExperimentOutput]))
    )

    val `linreg.train.flink` = new FlinkExperimentSysML(
      name    = "linreg.train.flink",
      command =
          s"""
             |-c org.apache.sysml.api.DMLScript \\
             |-C file://$${app.path.apps}/hadoop-mapreduce-client-jobclient-2.7.1.jar $${app.path.apps}/SystemML.jar \\
             |-f $${app.path.apps}/scripts/algorithms/LinearRegDS.dml -exec hybrid_flink -explain -nvargs \\
             |X=$${system.hadoop-2.path.output}/linRegData.train.data.bin \\
             |Y=$${system.hadoop-2.path.output}/linRegData.train.labels.bin \\
             |B=$${system.hadoop-2.path.output}/betas.csv fmt=csv
         """.stripMargin.trim,
      config = ConfigFactory.parseString(""),
      runs   = runs,
      runner = ctx.getBean("flink-1.0.3", classOf[Flink]),
      inputs = Set(ctx.getBean("linreg.dataset.features", classOf[DataSet]), ctx.getBean("linreg.dataset.features_split", classOf[DataSet])),
      outputs = Set(ctx.getBean("linreg.train.ds.output", classOf[ExperimentOutput]))
    )

    val `linreg.train.yarn` = new YarnExperiment(
      name    = "linreg.train.yarn",
      command =
          s"""
             |$${app.path.apps}/SystemML.jar \\
             |org.apache.sysml.api.DMLScript \\
             |-f $${app.path.apps}/scripts/algorithms/LinearRegDS.dml -explain -nvargs \\
             |X=$${system.hadoop-2.path.output}/linRegData.train.data.bin \\
             |Y=$${system.hadoop-2.path.output}/linRegData.train.labels.bin \\
             |B=$${system.hadoop-2.path.output}/betas.csv fmt=csv
         """.stripMargin.trim,
      config = ConfigFactory.parseString(""),
      runs   = runs,
      runner = ctx.getBean("yarn-2.7.1", classOf[Yarn]),
      inputs = Set(ctx.getBean("linreg.dataset.features", classOf[DataSet]), ctx.getBean("linreg.dataset.features_split", classOf[DataSet])),
      outputs = Set(ctx.getBean("linreg.train.ds.output", classOf[ExperimentOutput])),
      systems = Set()
    )

    new ExperimentSuite(Seq(
      `linreg.train.spark`,
      `linreg.train.flink`,
      `linreg.train.yarn`
    ))
  }

  @Bean(name = Array("linregCG.train.ds"))
  def `linregCG.train.ds`: ExperimentSuite = {
    val `linreg.train.spark` = new SparkExperiment(
      name    = "linreg.train.spark",
      command =
          s"""
             |--class org.apache.sysml.api.DMLScript \\
             |$${app.path.apps}/SystemML.jar \\
             |-f $${app.path.apps}/scripts/algorithms/LinearRegCG.dml -exec hybrid_spark -explain -nvargs \\
             |X=$${system.hadoop-2.path.output}/linRegData.train.data.bin \\
             |Y=$${system.hadoop-2.path.output}/linRegData.train.labels.bin \\
             |B=$${system.hadoop-2.path.output}/betas.csv maxi=6 fmt=csv
         """.stripMargin.trim,
      config = ConfigFactory.parseString(""),
      runs   = runs,
      runner = ctx.getBean("spark-1.6.0", classOf[Spark]),
      inputs = Set(ctx.getBean("linreg.dataset.features", classOf[DataSet]), ctx.getBean("linreg.dataset.features_split", classOf[DataSet])),
      outputs = Set(ctx.getBean("linreg.train.ds.output", classOf[ExperimentOutput]))
    )

    val `linreg.train.flink` = new FlinkExperimentSysML(
      name    = "linreg.train.flink",
      command =
          s"""
             |-c org.apache.sysml.api.DMLScript \\
             |-C file://$${app.path.apps}/hadoop-mapreduce-client-jobclient-2.7.1.jar $${app.path.apps}/SystemML.jar \\
             |-f $${app.path.apps}/scripts/algorithms/LinearRegCG.dml -exec hybrid_flink -explain -nvargs \\
             |X=$${system.hadoop-2.path.output}/linRegData.train.data.bin \\
             |Y=$${system.hadoop-2.path.output}/linRegData.train.labels.bin \\
             |B=$${system.hadoop-2.path.output}/betas.csv maxi=6 fmt=csv
         """.stripMargin.trim,
      config = ConfigFactory.parseString(""),
      runs   = runs,
      runner = ctx.getBean("flink-1.0.3", classOf[Flink]),
      inputs = Set(ctx.getBean("linreg.dataset.features", classOf[DataSet]), ctx.getBean("linreg.dataset.features_split", classOf[DataSet])),
      outputs = Set(ctx.getBean("linreg.train.ds.output", classOf[ExperimentOutput]))
    )

    val `linreg.train.yarn` = new YarnExperiment(
      name    = "linreg.train.yarn",
      command =
          s"""
             |$${app.path.apps}/SystemML.jar \\
             |org.apache.sysml.api.DMLScript \\
             |-f $${app.path.apps}/scripts/algorithms/LinearRegCG.dml -explain -nvargs \\
             |X=$${system.hadoop-2.path.output}/linRegData.train.data.bin \\
             |Y=$${system.hadoop-2.path.output}/linRegData.train.labels.bin \\
             |B=$${system.hadoop-2.path.output}/betas.csv maxi=6 fmt=csv
         """.stripMargin.trim,
      config = ConfigFactory.parseString(""),
      runs   = runs,
      runner = ctx.getBean("yarn-2.7.1", classOf[Yarn]),
      inputs = Set(ctx.getBean("linreg.dataset.features", classOf[DataSet]), ctx.getBean("linreg.dataset.features_split", classOf[DataSet])),
      outputs = Set(ctx.getBean("linreg.train.ds.output", classOf[ExperimentOutput])),
      systems = Set()
    )

    new ExperimentSuite(Seq(
      `linreg.train.spark`,
      `linreg.train.flink`,
      `linreg.train.yarn`
    ))
  }
}
