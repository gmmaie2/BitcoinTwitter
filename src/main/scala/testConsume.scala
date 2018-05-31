object  testConsume extends App {

  val kc = new KafkaMessageConsumer(args(0),"exchange")
  kc.consume()
}
