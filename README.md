# SystemML Benchmark Peel Bundle

This Peel bundle benchmarks specific versions of Apache SystemML for different backends: 
1. Flink-1.0.3
2. Spark-1.6.0
3. Yarn-2.7.1

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
mvn clean deploy -Pdev
```


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

So the first thing to do is to setup the HDFS (this will also format the namenode of the temporary hdfs):

```
./peel.sh sys:setup hdfs-2.7.1
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

## Dstat visualization
Generate charts from Dstat monitoring:
```
./peel.sh suite:vi linregDS.train.ds
./peel.sh suite:vi linregCG.train.ds

./peel.sh suite:vi linregDS.train.ds.provided
./peel.sh suite:vi linregCG.train.ds.provided

```
You can find all generated charts in: 
```
cd "$BUNDLE_BIN"/sysml-benchmark/charts
```
You can also generate charts which are comparable between all experiments within the suite using the parameter "-c":
```
./peel.sh suite:vi -c linregDS.train.ds
./peel.sh suite:vi -c linregCG.train.ds

./peel.sh suite:vi -c linregDS.train.ds.provided
./peel.sh suite:vi -c linregCG.train.ds.provided

```
