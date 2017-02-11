import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by marti on 11/02/2017.
  */
object PairsWord {

  def main(args: Array[String]): Unit = {

    val inputPath = new Path(args(0))
    val outputPath = new Path(args(1))


    val conf = new SparkConf()
      .setMaster("local[*]")
      .setAppName("words pairs occurences")
      .set("spark.executor.memory", "2g")

    val sc = new SparkContext(conf)

    val start = System.currentTimeMillis()

    println("Job Started :" + start)

    val data = sc.textFile(inputPath.toString)

    val pairs = data.flatMap{ line => line.split(",") // "word1,word2,word3" => Array(word1,word2,word3)
                                          .combinations(2) // Iterator
                                          .toSeq           // Array(Array(word1,word2), Array(word1,word3), Array(word2,word3))
                                          .map{ case list => list(0) -> list(1) } } // Array((word1,word2),(word1,word3),(word2,word3))

    // Array((word1,word2),(word1,word3),(word2,word3),...)

    val result = pairs.map( item => item -> 1) // Array(((word1,word2),1) ,((word1,word3),1),((word2,word3),1))
                      .reduceByKey(_+_)   // Array(((word1,word2),1) ,((word1,word3),1),((word2,word3),1),...)

    //val result = data.map(line => line.split(",")).flatMap(value => value.sliding(2)).map(tuple => (tuple(0), tuple(1)) -> 1).reduceByKey(_+_)

    val hadoopConfig = new Configuration()
    val hdfs = FileSystem.get(hadoopConfig)
    if (hdfs.exists(outputPath)) {
      hdfs.delete(outputPath, true)
    }

    result.foreach(println)

    result.saveAsTextFile(outputPath.toString)

    val end = System.currentTimeMillis()

    println("Job Ended :" + end)
    println("The job took " + (end - start) / 1000  + " seconds" )

    sc.stop()
  }



}
