# BitcoinTwitter
Real-time streaming application - pulls live twitter data on bitcoin and provides a sentiment score


### Step 1: start zookeeper service ###
 Modfiy Zookeeper.properties - provide absloute path to property `dataDir`
    ZookeeperServerSA.java src/main/resources/Zookeeper.properties
   

Step 2: start Kafka service
KafkaServerSA.java src/main/resources/Kafka.properties src/main/resources/log4j.properties
  modfiy Kafka.properties - provide absloute path to property 'log.dir'

Step 3: start pulling data from bitcoin exchanges
BitcoinExchange.java info.bitrich.xchangestream.gdax.GDAXStreamingExchange src/main/resources/Producer.properties exchange
  Alternative to first argument(GDAXStreamingExchange), you can also provide following argument: info.bitrich.xchangestream.bitfinex.BitfinexStreamingExchange

Step 4: start pulling #bitcoin data from twitter
TweetSA.java src/main/resources/Producer.properties src/main/resources/bird.properties src/main/resources/log4j.properties testTopic
  modify bird.properties - provide twitter credential information. 
  ***Note this step requires a twitter application account.If you do not have one you can follow the steps here: "http://docs.inboundnow.com/guide/create-twitter-application/" ***

Step 5: start spark to process twitter sentiment
SparkSentiment.scala src/main/resources/Producer.properties src/main/resources/spark.properties

Step 6: start websocket server to stream twitter and bitcoin data
WebSocketServerSA.java src/main/resources/WSConsumer.properties

Step 7: start web server to visualize data
WebServerSA.java src/main/resources/home.html

Step 8: open web browser and type localhost:8000 to visualize data

Enjoy!
