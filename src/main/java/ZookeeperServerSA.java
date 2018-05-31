import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;

import java.io.*;
import java.util.Properties;

public class ZookeeperServerSA {


    public static void main (String [] args){
        //load path for zookeeper configuration file
        String filePathZookeeper = args[0];

        System.out.println("Starting Zookeeper Server...");

        File fileZoo = new File(filePathZookeeper);
        InputStream isZoo = null;
        Properties propsZoo = new Properties();

        //ensure zookeeper config file exists
        if (fileZoo.exists()) {

            try {
                isZoo = new FileInputStream(fileZoo);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //load configuration files to properties variable
            try {
                propsZoo.load(isZoo);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            System.out.println("File " + filePathZookeeper + " not found!");
        }

        //set zookeeper properties
        QuorumPeerConfig quorumConfiguration = new QuorumPeerConfig();
        try {
            quorumConfiguration.parseProperties(propsZoo);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (QuorumPeerConfig.ConfigException e) {
            e.printStackTrace();
        }

        ZooKeeperServerMain zooKeeperServer = new ZooKeeperServerMain();
        ServerConfig configuration = new ServerConfig();
        configuration.readFrom(quorumConfiguration);

        //start zookeeper
        try {
            System.out.println("Zookeeper Server info:");
            System.out.println(" port: " + propsZoo.getProperty("clientPort"));
            System.out.println(" server: " + propsZoo.getProperty("server.1"));
            zooKeeperServer.runFromConfig((configuration));
        } catch (IOException e) {
            e.printStackTrace();
        }



    }
}

