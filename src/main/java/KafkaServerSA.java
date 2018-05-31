import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.util.Properties;

public class KafkaServerSA {


    public static void main(String[] args) {
        //Configure log4j
        PropertyConfigurator.configure(args[1]);
        //load path for kafka configuration file
        String filePathKafka = args[0];

        File fileKafka = new File(filePathKafka);
        InputStream isKafka = null;
        Properties propsKafka = new Properties();

        //ensure kafka config file exists
        if (fileKafka.exists()) {
            try {
                isKafka = new FileInputStream(fileKafka);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //load configuration files to properties variable
            try {
                propsKafka.load(isKafka);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            System.out.println("File " + filePathKafka + " not found!");
        }

       //start kafka server
        try {
            System.out.println("Loading Kafka Server...");
            System.out.println(propsKafka.getProperty("zookeeper.connect"));
            KafkaServerStartable myKafkaServer = new KafkaServerStartable(new KafkaConfig(propsKafka));
            myKafkaServer.startup();
            System.out.println("Started Kafka Server...");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
