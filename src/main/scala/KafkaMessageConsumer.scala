import java.io._
import java.util.{Date, Properties}
import collection.JavaConversions._
import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerRecords, KafkaConsumer}

class KafkaMessageConsumer(filePath:String,topic:String) extends java.io.Serializable {
  //load path for configuration file
  val file:File = new File(filePath)
  var is: InputStream =null;
  val props = new Properties()
  if(file.exists()){
    try {
      is = new FileInputStream(file)
    }catch{
      case e: FileNotFoundException =>
        e.printStackTrace()
    }

    try{
      props.load(is)
    }catch{
      case e: IOException =>
        e.printStackTrace()
    }
  }else{println("File " + filePath + " not found!")}
  //set kafka consumer properties
  val consumer = new KafkaConsumer[String, String](props)

  def consume(){
    var stop:Boolean=false
    var time = System.currentTimeMillis()

    try{
      //subscribe to kafka topic
      consumer.subscribe(java.util.Arrays.asList(this.topic))
    }
    catch{case e: Exception => e.printStackTrace();e.toString();println(e.toString)}

    while(!stop) {

      val records:ConsumerRecords[String, String] = consumer.poll(100)

      if (records.count() != 0){
        time = System.currentTimeMillis()

        for(record:ConsumerRecord[String,String] <-records){
          //print message consumed from kafka to console
          println("Value: " + record.value())
        }

      }else{
        if(System.currentTimeMillis()-time > 30000){
          //will loop through and if no data is
          //processesd within 30sec it will end loop
          //and close consumer. if data is processed
          // within 10sec then timer will reset to 1sec
          println("Consumer timed out...")
          stop = true
        }
      }
    }
    consumer.close()
  }

}
