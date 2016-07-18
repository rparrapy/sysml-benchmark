package config.fixtures

import com.samskivert.mustache.Mustache
import com.samskivert.mustache.Mustache.Compiler
import org.peelframework.core.beans.system.Lifespan
import org.peelframework.flink.beans.system.Flink
import org.peelframework.hadoop.beans.system.{HDFS2, Yarn}
import org.peelframework.spark.beans.system.Spark
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.context.{ApplicationContext, ApplicationContextAware}

/** System beans for the 'sysml-benchmark' bundle. */
@Configuration
class systems extends ApplicationContextAware {

  /* The enclosing application context. */
  var ctx: ApplicationContext = null

  def setApplicationContext(ctx: ApplicationContext): Unit = {
    this.ctx = ctx
  }

  // ---------------------------------------------------
  // Systems
  // ---------------------------------------------------

  @Bean(name = Array("flink-1.0.3"))
  def `flink-1.0.3`: Flink = new Flink(
    version      = "1.0.3",
    configKey    = "flink",
    lifespan     = Lifespan.EXPERIMENT,
    dependencies = Set(ctx.getBean("hdfs-2.7.1", classOf[HDFS2])),
    //dependencies = Set(),
    mc           = ctx.getBean(classOf[Mustache.Compiler])
  )

  @Bean(name = Array("spark-1.6.0"))
  def `spark-1.6.0`: Spark = new Spark(
    version      = "1.6.0",
    configKey    = "spark",
    lifespan     = Lifespan.EXPERIMENT,
    dependencies = Set(ctx.getBean("hdfs-2.7.1", classOf[HDFS2])),
    //dependencies = Set(),
    mc           = ctx.getBean(classOf[Mustache.Compiler])
  )

  @Bean(name = Array("yarn-2.7.1"))
  def `yarn-2.7.1`: Yarn = new Yarn(
    version      = "2.7.1",
    configKey    = "hadoop-2",
    lifespan     = Lifespan.EXPERIMENT,
    dependencies = Set(ctx.getBean("hdfs-2.7.1", classOf[HDFS2])),
    //dependencies = Set(),
    mc           = ctx.getBean(classOf[Mustache.Compiler])
  )


  @Bean(name = Array("hdfs-2.7.1"))
  def `hdfs-2.7.1`: HDFS2 = new HDFS2(
    version      = "2.7.1",
    configKey    = "hadoop-2",
    lifespan     = Lifespan.SUITE,
    dependencies = Set(),
    mc           = ctx.getBean(classOf[Compiler])
  )
}