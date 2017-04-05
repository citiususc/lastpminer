
How to run the algorithms?
==============================
1. Set up src/config/config.properties. There is an example file inside the folder where the possible parameters are explained.
2. Identify which parameters need to be set. For a further explanation check class 'Principal'. The most relevant are:

  * algorithm [**ASTP** | LASTP | ...]
  * mode [**BASIC** | EPISODE | SEED | FULL]
  * windowSize [**80**, int]
  * minFreq [**30**, int]
  * collection [**apnea** | BD4 | BD5 | BDR56 | ... ]
  * iterations [**1**, int ]

Full means that the algorithm uses both the episode definitions and the seed pattern. Only the SAHS collection (AKA "apnea") can use the seed pattern. 

3. Run the java command setting the classpath and JVM arguments.

Example:

`java -cp /path/to/hstpminer.jar:/path/to/lib/* -Xms512m -Xmx4g -XX:-UseGCOverheadLimit -XX:+UseConcMarkSweepGC  source.Principal algorithm=wm mode=basic windowSize=20 minFreq=300 collection="BDRoE6" iterations=5 resultPath="/path/to/results/BDRoE6/" writeReport=true writeMarkingReport=true writeIterationsReport=true`

