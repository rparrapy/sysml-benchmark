# SystemML Benchmark Peel Bundle

This Peel bundle benchmarks specific versions of Apache SystemML for different backends: 
1) Flink-1.0.3
2) Spark-1.6.0
3) Yarn-2.7.1

## Peel Bundle Setup
For Peel we always use two directories - one to develop bundles and one to run the bundles. 
So we define the two directories as variables here:

```
export BUNDLE_SRC=/home/neutatz
export BUNDLE_BIN=/home/hadoop/bundles
```

So we change into our development folder and download the benchmark bundle:

```
cd "$BUNDLE_SRC"
git clone https://github.com/fschueler/sysml-benchmark.git
```

Now we specify which version (branch) of SystemML we want to benchmark. 
To do this we have to configure the following script:

```
vi "$BUNDLE_SRC"/sysml-benchmark/sysml-benchmark-bundle/src/main/resources/utils/sync-sysml.sh
```

In this file you have to configure the following lines:

```
# Github data
USER=stratosphere
REPO=incubator-systemml
BRANCH=flink-lr
```

Now we build this version by executing the script:
```
sh "$BUNDLE_SRC"/sysml-benchmark/sysml-benchmark-bundle/src/main/resources/utils/sync-sysml.sh
```

Moreover we have to provide the configuration for our environment. You can find an example in this directory:
```
cd "$BUNDLE_SRC"/sysml-benchmark/sysml-benchmark-bundle/src/main/resources/config/hosts
```

The next step is to build the Peel bundle:
```
cd "$BUNDLE_SRC"/sysml-benchmark
mvn clean deploy
```

<!--
Now everything is built. So we need to copy the Peel bundle to the folder where we actually run it.
```
mkdir "$BUNDLE_BIN"/sysml-benchmark # create directory if not yet existent
rm -R "$BUNDLE_BIN"/sysml-benchmark/* # clean directory
cp -R "$BUNDLE_SRC"/sysml-benchmark/sysml-benchmark-bundle/target/sysml-benchmark/* "$BUNDLE_BIN"/sysml-benchmark/
cp -R "$BUNDLE_SRC"/sysml-benchmark/sysml-benchmark-bundle/src/main/resources/apps/* "$BUNDLE_BIN"/sysml-benchmark/apps/
```
-->

## Peel Bundle Execution

The execution will only take place in the _BUNDLE_BIN_ we defined. So we change to this folder first:
```
cd "$BUNDLE_BIN"/sysml-benchmark
```

We provide two execution modi. These modi differ in the generation of the dataset. 

### User modus
In this modus we will generate the data for every suite once. 
This means once the suite is finished we also delete the data set.
The commands to execute the available suites are:
```
./peel.sh suite:run linregDS.train.ds
./peel.sh suite:run linregCG.train.ds
```
These two suites will execute two different versions of linear regression. 
The underlying experiments are the following:
```
#linregDS.train.ds
./peel.sh exp:run linregDS.train.ds linreg.train.spark
./peel.sh exp:run linregDS.train.ds linreg.train.flink
./peel.sh exp:run linregDS.train.ds linreg.train.yarn

#linregCG.train.ds
./peel.sh exp:run linregCG.train.ds linreg.train.spark
./peel.sh exp:run linregCG.train.ds linreg.train.flink
./peel.sh exp:run linregCG.train.ds linreg.train.yarn

```

### Dev modus
In this modus we generate the data only once as long the temporary HDFS is not used by other Peel users.

So the first thing to do is to setup the HDFS:

```
./peel.sh system:setup hdfs-2.7.1-provided
```

Once HDFS is running we can run the suites:
```
./peel.sh suite:run linregDS.train.ds.provided
./peel.sh suite:run linregCG.train.ds.provided
```

Or the corresponding experiments:
```
#linregDS.train.ds.provided
./peel.sh exp:run linregDS.train.ds.provided linreg.train.spark
./peel.sh exp:run linregDS.train.ds.provided linreg.train.flink
./peel.sh exp:run linregDS.train.ds.provided linreg.train.yarn

#linregCG.train.ds.provided
./peel.sh exp:run linregCG.train.ds.provided linreg.train.spark
./peel.sh exp:run linregCG.train.ds.provided linreg.train.flink
./peel.sh exp:run linregCG.train.ds.provided linreg.train.yarn

```

After you are finished developing, you can shutdown the HDFS using:
```
./peel.sh system:teardown hdfs-2.7.1-provided
```
But be careful, this will delete the data!! If you want to prevent that and keep the data, you have to change the parameter `system.hadoop-2.format` to `false` in the `hadoop-2.conf` of your environment.
