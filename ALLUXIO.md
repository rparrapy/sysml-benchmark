# Alluxio Integration

Alluxio is an open source in-memory HDFS implementation, thus enabling faster data access for applications with minimal configuration overhead.

Significant performance improvements can be achieved for the Flink-SystemML benchmark by using Alluxio to store the input and output datasets.
This guide describes the step needed to achieve this.

## Alluxio Installation
1. Download Alluxio from its [download page](http://www.alluxio.org/download). 
From here on we assume Alluxio 1.5.0 as it the latest released version at the moment of writing this guide.
2. Unpack it and enter the newly created directory:
  
    ```
    tar -xzf alluxio-1.5.0-bin.tar.gz && cd alluxio-1.5.0
    ```

3. Create a new configuration file from the available template: 

    ```
    cp conf/alluxio-site.properties.template conf/alluxio-site.properties
    ```

4. Update ```alluxio.master.hostname``` in ```conf/alluxio-site.properties``` to the hostname of the machine you plan to run Alluxio Master on.
Adjust the memory settings for each worker by updating the correspending properties in this file.

5. Add the IP addresses or hostnames of all the worker nodes to the ```conf/workers``` file.

6. Add ```client/hadoop/alluxio-1.5.0-hadoop-client.jar``` to Hadoop's classpath and ```client/flink/alluxio-1.5.0-flink-client.jar``` 
to Flink's classpath.

7. Format and start Alluxio:
    ```
    ./bin/alluxio format
    ./bin/alluxio-start.sh all NoMount
    ```
    
## Benchmark configuration
1. Change the repository/branch of SystemML to test in ```"$BUNDLE_SRC"/sysml-benchmark/sysml-benchmark-bundle/src/main/resources/utils/sync-sysml.sh``` to:

    ```
    # Github data
    USER=rparrapy
    REPO=incubator-systemml
    BRANCH=flink-lr
    ```
2. Change the ```hadoop2.conf``` configuration file to use Alluxio as a storage. The location of this file and the property values depend on which cluster is being used. 
The following property-value pairs work for the ibm-power-1 cluster.

    ```
    system.hadoop-2.path.input="alluxio://ibm-power-1:19998/tmp/input"
    system.hadoop-2.path.output="alluxio://ibm-power-1:19998/tmp/output"
    system.hadoop-2.config.core.fs.alluxio.impl="alluxio.hadoop.FileSystem"
    system.hadoop-2.config.core.fs.alluxio-ft.impl="alluxio.hadoop.FaultTolerantFileSystem"
    system.hadoop-2.config.core.fs.AbstractFileSystem.alluxio.impl="alluxio.hadoop.AlluxioFileSystem"
    ```
3. Run experiments as usual.
