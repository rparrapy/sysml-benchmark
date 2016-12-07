package eu.stratosphere.benchmarks.systemml.cli.command.visualizationUtil

/**
 * Created by felix on 10.10.16.
 */
class Category(namec: String, start: Int, divisorc: Double) {

  val name = namec
  val startID = start
  var endID = -1
  var divisor = divisorc
  
  def this(namec: String, start: Int) {
    this(namec, start, 1)
    
    if (namec.equals("memory_usage") 
        || namec.equals("dsk_total") 
        || namec.equals("net_total")
        || namec.equals("most_expensive")) {
      divisor = 1024 * 1024 * 1024 //show in GB
    }
    endID = start
  }
  
  
}
