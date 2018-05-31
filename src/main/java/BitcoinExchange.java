import com.google.gson.JsonObject;
import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;


public class BitcoinExchange {
    public static void main(String[] args){
        //StreamingExchange exchange = StreamingExchangeFactory.INSTANCE.createExchange(GDAXStreamingExchange.class.getName());
        //args options for 0:
        //info.bitrich.xchangestream.gdax.GDAXStreamingExchange
        //info.bitrich.xchangestream.bitfinex.BitfinexStreamingExchange
        //info.bitrich.xchangestream.bitflyer.BitflyerStreamingExchange
        //info.bitrich.xchangestream.bitstamp.BitstampStreamingExchange - Requested Information from Exchange is not available
        //info.bitrich.xchangestream.poloniex.PoloniexStreamingExchange - Failed
        //info.bitrich.xchange.coinmate.CoinmateStreamingExchange - Requested Information from Exchange is not available.
        //info.bitrich.xchangestream.okcoin.OkCoinStreamingExchange - works but null timestamp and with Errors
        //info.bitrich.xchangestream.binance.BinanceStreamingExchange - Requested Information from Exchange is not available.
        //info.bitrich.xchangestream.gemini.GeminiStreamingExchange -Errors
        //info.bitrich.xchangestream.poloniex2.PoloniexStreamingExchange - no output. Freeze?
        //info.bitrich.xchangestream.wex.WexStreamingExchange - Error - Null point execption

        //create kafka producer
        KafkaMessageProducer kafka = new KafkaMessageProducer(args[1],args[2]);//(server, kafka topic)
        //select exchange instance
        StreamingExchange exchange = StreamingExchangeFactory.INSTANCE.createExchange(args[0]);
        //build exchange connect type
        ProductSubscription typeConnect = ProductSubscription.create().addTicker(CurrencyPair.BTC_USD).build();
        //initalize connection
        exchange.connect(typeConnect).blockingAwait();
        //start streaming data from exchange
        exchange.getStreamingMarketDataService()
                .getTicker(CurrencyPair.BTC_USD)
                .subscribe(trade -> {
                    System.out.println(args[0] + ": " + trade);
                    //convert exchange data into json and send to kafka
                    kafka.sendMessage(tickerToJSON(trade,args[0]).toString());

                }, throwable -> {
                    System.out.println("Error in subscribing trades: " + throwable);

                });



    }

    public static JsonObject tickerToJSON(Ticker data, String Exchange) {
        JsonObject dataJson = new JsonObject();

        dataJson.addProperty("exchange", Exchange.substring(Exchange.lastIndexOf(".")+1,Exchange.length()));
        dataJson.addProperty("curr", java.util.Objects.toString(data.getCurrencyPair(),"null"));
        dataJson.addProperty("last", java.util.Objects.toString(data.getLast(),"null").toString());
        dataJson.addProperty("bid", java.util.Objects.toString(data.getBid(),"null").toString());
        dataJson.addProperty("ask", java.util.Objects.toString(data.getAsk(),"null").toString());
        dataJson.addProperty("high", java.util.Objects.toString(data.getHigh(),"null").toString());
        dataJson.addProperty("low", java.util.Objects.toString(data.getLow(),"null").toString());
        dataJson.addProperty("volume", java.util.Objects.toString(data.getVolume(),"null").toString());
        dataJson.addProperty("timestamp", java.util.Objects.toString(data.getTimestamp().getTime(),"null"));
        System.out.println("TIME###############33");
        System.out.println(data.getTimestamp());
        System.out.println(data.getTimestamp().getTime());

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("crypto", dataJson);

        return jsonObject;
    }



}

