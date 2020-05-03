# Cluster Run

If you want to run this via spark-submit on the SJSU cluster:
```shell script
cd ~/Developer/github.com/AndrewSelviaSJSU/cmpe256-ghost-towns/preparer
sbt assembly
scp target/scala-2.11/preparer-assembly-0.1.0-SNAPSHOT.jar 014547273@10.31.20.1:~/cmpe256/ghost-towns/preparer-assembly-0.1.0-SNAPSHOT.jar
```

Then, get on the cluster:
```shell script
ssh 014547273@coe-hpc1.sjsu.edu
```

Run your job:
```shell script
cd cmpe256/ghost-towns
sbatch ghost-towns.sh --class edu.sjsu.cmpe256.ghost_towns.Preparer preparer-assembly-0.1.0-SNAPSHOT.jar
```

To see the progress:
```shell script
squeue
squeue -j 21631
```

See the output, update the file name to the name of your job.
```shell script
cat sparkjob-21606.out
```