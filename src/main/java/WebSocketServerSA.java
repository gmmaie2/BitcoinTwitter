/*
 * Copyright (c) 2010-2018 Nathan Rajlich
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 */

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class WebSocketServerSA extends WebSocketServer {
    private static int port = 8887;
    private static String filePath ="";

    public WebSocketServerSA(int port, String filePath ) throws UnknownHostException {
        super( new InetSocketAddress( port ) );
        this.port = port;
        this.filePath = filePath;
    }

    public WebSocketServerSA(InetSocketAddress address ) {
        super( address );
    }

    @Override
    public void onOpen( WebSocket conn, ClientHandshake handshake ) {
        //Prints when new user joins
        System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!" );
    }

    @Override
    public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
        //Prints when user leaves
        System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + " has left the room!" );
    }

    @Override
    public void onMessage( WebSocket conn, String message ) {
        //prints all messages received from clients
        System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + ": " + message );

    }

    @Override
    public void onError( WebSocket conn, Exception ex ) {
        ex.printStackTrace();
        if( conn != null ) {
            System.out.println(conn.getRemoteSocketAddress().getAddress().getHostAddress() + ": " + ex.getMessage());
        }
    }

    @Override
    public void onStart() {
        System.out.println("=====================================");
        //load path for Kafka Consumer configuration file
        File file = new File(filePath);
        InputStream is = null;
        Properties props = new Properties();
        //if Kafka Consumer configuration file exists will load into properties variable
        if(file.exists()) {
            try {
                is = new FileInputStream(filePath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            try {
                props.load(is);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else{ System.out.println("File " + filePath + " not found!");}

        //Creates Kafka Consumer
        KafkaConsumer consumer = new KafkaConsumer<String,String>(props);
        //Kafka Consumer subscribing to topics
        consumer.subscribe(java.util.Arrays.asList("exchange","sentimentTopic"));
        //Executor created to manage thread
        ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
        //thread to execute Kafka consumer at fixed interval
        timer.scheduleAtFixedRate(() -> consume(consumer),0,1, TimeUnit.MILLISECONDS);

        System.out.println("Websocket Server started at time: " + System.currentTimeMillis());

    }

    public void consume(KafkaConsumer consumer){
        ConsumerRecords <String,String>records = consumer.poll(100);
        for(ConsumerRecord record:records){
            //Kafka consumer retrieving data from kafka and sending via websocket
            broadcast(record.value().toString());
        }
    }

    public static void main(String[] args) {

        System.out.println("Websocket Server starting...");
        WebSocketImpl.DEBUG = true;

        try {
            port = Integer.parseInt(String.valueOf(port));
        } catch (Exception ex) {
        }
        WebSocketServerSA s = null;
        //Initializing websocket server
        try {
            s = new WebSocketServerSA(port, args[0]);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        //Starting websocket server
        s.start();

    }
}
