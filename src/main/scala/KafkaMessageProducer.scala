//packge name of folder here
import java.io._
import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
class KafkaMessageProducer (filePath:String,topic:String) extends java.io.Serializable{
  //load path for configuration file
  println("Loading Kafka Producer...")
  private val props = new Properties()
  val file = new File(filePath)
  var is: InputStream = null
  if (file.exists()) {
    try {
      is = new FileInputStream(filePath)
    }catch {
      case e: FileNotFoundException =>
        e.printStackTrace()
    }

    try {
      props.load(is)
    }catch {
      case e: IOException =>
        e.printStackTrace()
    }
  } else{println("File " + filePath + " not found!")}
  //set kafka consumer properties
  private val kafkaProducer = new KafkaProducer[String,String](props)

  println("Started Kafka Producer...")
  def sendMessage(data:String):Unit={
    println("Building message packet...")
    val record = new ProducerRecord[String,String](topic, Long.MaxValue.toString, data)
    try{
      //sends message to kafka
      kafkaProducer.send(record)
      println("Sent message...")
    }catch{case e : Exception => e.printStackTrace();e.toString();println(e.toString)}
  }

  def endMessage(): Unit ={
    kafkaProducer.close()
  }

}
