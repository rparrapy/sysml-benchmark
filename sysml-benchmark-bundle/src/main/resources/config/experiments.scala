package config

import com.typesafe.config.ConfigFactory
import org.peelframework.core.beans.data.ExperimentOutput
import org.peelframework.core.beans.experiment.ExperimentSuite
import org.peelframework.hadoop.beans.experiment.YarnExperiment
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

  /* The enclosing application context. */
  var ctx: ApplicationContext = null

  def setApplicationContext(ctx: ApplicationContext): Unit = {
    this.ctx = ctx
  }

  // ---------------------------------------------------
  // Experiments
  // ---------------------------------------------------

  @Bean(name = Array("linreg.data.generate"))
  def `linreg.data.generate`: ExperimentSuite = {
    val `linreg.data.generate.features` = new YarnExperiment(
      command =
        s"""
           |$${app.path.apps}/SystemML.jar \\
           |org.apache.sysml.api.DMLScript -f $${app.path.apps}/scripts/datagen/genLinearRegressionData.dml \\
           |-nvargs numSamples=1000 numFeatures=50 maxFeatureValue=5 maxWeight=5 \\
           |addNoise=FALSE b=0 sparsity=0.7 output=$${system.hadoop-2.path.output}/linreg/linRegData.csv format=csv perc=0.5
        """.stripMargin.trim,
      systems = Set(),
      runner  = ctx.getBean("yarn-2.7.1", classOf[Yarn]),
      runs    = runs,
      inputs  = Set(),
      outputs = Set(ctx.getBean("linreg.output.features", classOf[ExperimentOutput])),
      name    = "linreg.data.generate.features",
      config  = ConfigFactory.parseString("")
    )

    val `linreg.data.sample.features` = new YarnExperiment(
      command =
        s"""
           |$${app.path.apps}/SystemML.jar \\
           |org.apache.sysml.api.DMLScript -f $${app.path.apps}/scripts/utils/sample.dml \\
           |-nvargs X=output=$${system.hadoop-2.path.output}/linreg/linRegData.csv \\
           |sv=output=$${system.hadoop-2.path.output}/linreg/perc.csv \\
           |O=output=$${system.hadoop-2.path.output}/linreg/linRegDataParts ofmt=csv
         """.stripMargin.trim,
      systems = Set(),
      runner  = ctx.getBean("yarn-2.7.1", classOf[Yarn]),
      runs    = runs,
      inputs  = Set(),
      outputs = Set(ctx.getBean("linreg.output.samples", classOf[ExperimentOutput])),
      name    = "linreg.data.sample.features",
      config  = ConfigFactory.parseString("")
    )

    new ExperimentSuite(Seq(
    `linreg.data.generate.features`,
    `linreg.data.sample.features`
    ))
  }
}