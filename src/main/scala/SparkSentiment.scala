import java.io._

import org.apache.kafka.clients.producer.ProducerConfig
import java.util.Properties

import com.google.gson.JsonObject
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming._
//import org.apache.spark.streaming.kafka._
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import _root_.kafka.serializer.StringDecoder

import scala.util.parsing.json._
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import java.util.concurrent.Future

import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.spark.broadcast.Broadcast

//kafka 0.10
import org.apache.kafka.clients.consumer.ConsumerRecord
//spark-streaming-kafka 2.1.0
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

object SparkSentiment {

  def main(args: Array[String]): Unit = {
    //load path for configuration file
    val producerProp = new Properties()
    val producerFile = new File(args(0))
    var producerIs: InputStream = null
    if (producerFile.exists()) {
      try {
        producerIs = new FileInputStream(args(0))
      }catch {
        case e: FileNotFoundException =>
          e.printStackTrace()
      }

      try {
        producerProp.load(producerIs)
      }catch {
        case e: IOException =>
          e.printStackTrace()
      }
    } else{println("File " + args(0) + " not found!")}
    //load path for configuration file
    val sparkProp = new Properties()
    val sparkFile = new File(args(1))
    var sparkIs: InputStream = null
    if (sparkFile.exists()) {
      try {
        sparkIs = new FileInputStream(args(1))
      }catch {
        case e: FileNotFoundException =>
          e.printStackTrace()
      }

      try {
        sparkProp.load(sparkIs)
      }catch {
        case e: IOException =>
          e.printStackTrace()
      }
    } else{println("File " + args(1) + " not found!")}
    //configuring spark
    val ssc: StreamingContext = {
      val sparkConf = new SparkConf().setAppName(sparkProp.getProperty("setAppName")).setMaster(sparkProp.getProperty("setMaster"))
      new StreamingContext(sparkConf, Seconds(sparkProp.getProperty("spark.streaming.seconds").toLong))
    }
    //configuring topic
    val topicSet = sparkProp.getProperty("topics").split(",").toSet
    //configuring kafka parameters
    val kafkaParams = Map[String, String](
      "bootstrap.servers" -> sparkProp.getProperty("bootstrap.servers"),
      "key.deserializer" -> sparkProp.getProperty("key.serializer"),
      "group.id" -> sparkProp.getProperty("group.id"),
      "value.deserializer" -> sparkProp.getProperty("value.serializer")
    )

    val consumerrecord:InputDStream[ConsumerRecord[String,String]]= KafkaUtils.createDirectStream(ssc,PreferConsistent,Subscribe[String, String](topicSet, kafkaParams))
    val messages = consumerrecord.map(record => (record.key, record.value))

    val sentimentEnriched = messages.map(_._2).map(r => {
      val jsonWrapper = JSON.parseFull(r).get.asInstanceOf[Map[String, Any]]
      val jsonData = jsonWrapper.get(jsonWrapper.keys.toList(0)).get.asInstanceOf[Map[String, Any]]
      println(jsonData.keys.toList(0).toString())
      //process one way if message is from youtube else process another way if message from twitter
      //add sentiment if messge from twitter
      if (jsonData.keys.toList(0).toString() == "liveChatId") {
        (jsonWrapper.keys.toList(0),
          jsonData.getOrElse("liveChatId", "null"),
          jsonData.getOrElse("timestamp", "null"),
          jsonData.getOrElse("AuthorChannelId", "null"),
          jsonData.getOrElse("DisplayName", "null"),
          jsonData.getOrElse("DisplayMessage", "null"),
          SentimentAnalysisUtils.getInstance().detectSentiment(jsonData.getOrElse("DisplayMessage", "null").toString))
      }
      else {
        (jsonWrapper.keys.toList(0),
          jsonData.getOrElse("Id", "null"),
          jsonData.getOrElse("ScreenName", "null"),
          jsonData.getOrElse("Name", "null"),
          jsonData.getOrElse("Email", "null"),
          jsonData.getOrElse("TimeZone", "null"),
          jsonData.getOrElse("Source", "null"),
          jsonData.getOrElse("isRetweet", "null"),
          jsonData.getOrElse("Tweet", "null"),
          jsonData.getOrElse("timestamp", "null"),
          jsonData.getOrElse("HashTagSize", "null"),
          jsonData.getOrElse("URLEntitiesSize", "null"),
          jsonData.getOrElse("RetweetCount", "null"),
          jsonData.getOrElse("FavoriteCount", "null"),
          jsonData.getOrElse("MediaEntitiesSize", "null"),
          jsonData.getOrElse("SymbolEntitiesSize", "null"),
          jsonData.getOrElse("isFavorited", "null"),
          jsonData.getOrElse("isRetweeted", "null"),
          jsonData.getOrElse("GeoLocation", "null"),
          jsonData.getOrElse("UserMentionEntitiesSize", "null"),
          jsonData.getOrElse("Lang", "null"),
          SentimentAnalysisUtils.getInstance().detectSentiment(jsonData.getOrElse("Tweet", "null").toString))//scores sentiment value for tweet
      }
    })
    //send kafka producer class to all worker nodes
    val kafkaProducer: Broadcast[MySparkKafkaProducer[String, String]] = {
      ssc.sparkContext.broadcast(MySparkKafkaProducer[String, String](producerProp))
    }

    //send processed message back to kafka
    sentimentEnriched.foreachRDD { rdd =>
      rdd.foreachPartition { partitionOfRecords =>
        val metadata: Stream[Future[RecordMetadata]] = partitionOfRecords.map { record =>
          val dataJson = sentimentDataToJSON(record.productIterator.toList)
          kafkaProducer.value.send("sentimentTopic", dataJson.toString)
        }.toStream
        metadata.foreach { metadata => metadata.get() }
      }
    }
    //start spark streaming
    ssc.start()
    ssc.awaitTermination()
  }
  // function to transform list into Json
  def sentimentDataToJSON( listData: List[Any]): JsonObject = {
    val dataJson = new JsonObject()
    if(listData(0).toString()== "Youtube"){
      dataJson.addProperty("liveChatId", java.util.Objects.toString(listData(1), "null").toString)
      dataJson.addProperty("timestamp", java.util.Objects.toString(listData(2), "null").toString)
      dataJson.addProperty("AuthorChannelId", java.util.Objects.toString(listData(3), "null").toString)
      dataJson.addProperty("DisplayName", java.util.Objects.toString(listData(4), "null").toString)
      dataJson.addProperty("DisplayMessage", java.util.Objects.toString(listData(5), "null").toString)
      dataJson.addProperty("sentiment", java.util.Objects.toString(listData(6), "null").toString)
    }else{
      dataJson.addProperty("Id",java.util.Objects.toString(listData(1), "null").toString)
      dataJson.addProperty("ScreenName",java.util.Objects.toString(listData(2), "null").toString)
      dataJson.addProperty("Name",java.util.Objects.toString(listData(3), "null").toString)
      dataJson.addProperty("Email",java.util.Objects.toString(listData(4), "null").toString)
      dataJson.addProperty("TimeZone",java.util.Objects.toString(listData(5), "null").toString)
      dataJson.addProperty("Source",java.util.Objects.toString(listData(6), "null").toString)
      dataJson.addProperty("isRetweet",java.util.Objects.toString(listData(7), "null").toString)
      dataJson.addProperty("Tweet",java.util.Objects.toString(listData(8), "null").toString)
      dataJson.addProperty("timestamp",java.util.Objects.toString(listData(9), "null").toString)
      dataJson.addProperty("HashTagSize",java.util.Objects.toString(listData(10), "null").toString)
      dataJson.addProperty("URLEntitiesSize",java.util.Objects.toString(listData(11), "null").toString)
      dataJson.addProperty("RetweetCount",java.util.Objects.toString(listData(12), "null").toString)
      dataJson.addProperty("FavoriteCount",java.util.Objects.toString(listData(13), "null").toString)
      dataJson.addProperty("MediaEntitiesSize",java.util.Objects.toString(listData(14), "null").toString)
      dataJson.addProperty("SymbolEntitiesSize",java.util.Objects.toString(listData(15), "null").toString)
      dataJson.addProperty("isFavorited",java.util.Objects.toString(listData(16), "null").toString)
      dataJson.addProperty("isRetweeted",java.util.Objects.toString(listData(17), "null").toString)
      dataJson.addProperty("GeoLocation",java.util.Objects.toString(listData(18), "null").toString)
      dataJson.addProperty("UserMentionEntitiesSize",java.util.Objects.toString(listData(19), "null").toString)
      dataJson.addProperty("Lang",java.util.Objects.toString(listData(20), "null").toString)
      dataJson.addProperty("sentiment",java.util.Objects.toString(listData(21), "null").toString)
    }
    val jsonObject = new JsonObject
    jsonObject.add(listData(0).toString(), dataJson)
    jsonObject
  }


}

