package eu.stratosphere.benchmarks.systemml.cli.command.visualizationUtil

/**
 * Created by felix on 10.10.16.
 */
class Metric(IDc: Int, namec: String, categoryc: Category) {
  val id: Int = IDc
  val name: String = namec
  val category: Category = categoryc
  
  def this(IDc: Int, namec: String) {
    this(IDc, namec, null)
  }
  
  def isEpoch(): Boolean = {
    namec.equals("epoch")
  }
}
