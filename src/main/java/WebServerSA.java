import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;

public class WebServerSA {
    private static String  filePath ="";
    public static void main(String[]args){
        //absolute file location of html file to display
        filePath = args[0];
        HttpServer server = null;
        //create http server
        try {
            server = HttpServer.create(new InetSocketAddress(8000), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.createContext("/", new WebServerSA.MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("HTPServer started at time: " + System.currentTimeMillis());
    }
    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            //"This is the response";
            String response = "";
            System.out.println("HTPServer: user " + t.getRemoteAddress() + " requesting " + t.getRequestURI());
            InputStream fis = new FileInputStream(filePath);
            Reader ff = new InputStreamReader(fis);
            //read html into response variable
            int s;
            while((s=ff.read()) != -1){
                response += (char) s;
            }
            //send response variable to requester
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
