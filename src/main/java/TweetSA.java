import com.google.gson.JsonObject;
import org.apache.log4j.PropertyConfigurator;
import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.io.*;
import java.util.Arrays;
import java.util.Properties;

public class TweetSA {

    public static void main(String[] args) {
        //load path for kafka producer config file
        String filePathProducer = args[0];
        //load path for twitter config file
        String filePathCred = args[1];
        //provide kafka topic name for tweets
        String topic = args[3];

        //load path for log4j config file
        PropertyConfigurator.configure(args[2]);
        System.out.println("Twitter Starting...");

        //create listener for twitter stream
        StatusListener listener = new StatusListener() {
                //creates a kafka producer
                KafkaMessageProducer kafka = new KafkaMessageProducer(filePathProducer, topic);

                @Override
                public void onStatus(Status status) {
                    //process tweet and store in json object
                    String tweet = "";
                    if (status.isRetweet() == true) {
                        tweet = status.getRetweetedStatus().getText().replace("\n", "\\n").replace(",", "`").toString();
                    } else {
                        tweet = status.getText().replace("\n", "\\n").replace(",", "`").toString();
                    }

                    JsonObject dataJson = new JsonObject();
                    dataJson.addProperty("Id", java.util.Objects.toString(status.getId(), "null"));
                    dataJson.addProperty("ScreenName", java.util.Objects.toString(status.getUser().getScreenName(), "null"));
                    dataJson.addProperty("Name", java.util.Objects.toString(status.getUser().getName(), "null").toString());
                    dataJson.addProperty("Email", java.util.Objects.toString(status.getUser().getEmail(), "null").toString());
                    dataJson.addProperty("TimeZone", java.util.Objects.toString(status.getUser().getTimeZone(), "null").toString());
                    dataJson.addProperty("Source", java.util.Objects.toString(status.getSource().replace("\"", "'"), "null").toString());
                    dataJson.addProperty("isRetweet", java.util.Objects.toString(status.isRetweet(), "null").toString());
                    dataJson.addProperty("Tweet", java.util.Objects.toString(tweet.replace("\"", "'").toLowerCase(), "null").toString());
                    dataJson.addProperty("timestamp", java.util.Objects.toString(new convertTime(status.getCreatedAt().toString()).toUnixTimestamp(), "null"));
                    dataJson.addProperty("HashTagSize", java.util.Objects.toString(status.getHashtagEntities().length, "null").toString());
                    dataJson.addProperty("URLEntitiesSize", java.util.Objects.toString(status.getURLEntities().length, "null").toString());
                    dataJson.addProperty("RetweetCount", java.util.Objects.toString(status.getRetweetCount(), "null").toString());
                    dataJson.addProperty("FavoriteCount", java.util.Objects.toString(status.getFavoriteCount(), "null").toString());
                    dataJson.addProperty("MediaEntitiesSize", java.util.Objects.toString(status.getMediaEntities().length, "null").toString());
                    dataJson.addProperty("SymbolEntitiesSize", java.util.Objects.toString(status.getSymbolEntities().length, "null").toString());
                    dataJson.addProperty("isFavorited", java.util.Objects.toString(status.isFavorited(), "null").toString());
                    dataJson.addProperty("isRetweeted", java.util.Objects.toString(status.isRetweeted(), "null").toString());
                    dataJson.addProperty("isTruncated", java.util.Objects.toString(status.isTruncated(), "null").toString());
                    dataJson.addProperty("isPossiblySensitive", java.util.Objects.toString(status.isPossiblySensitive(), "null").toString());
                    dataJson.addProperty("GeoLocation", java.util.Objects.toString(status.getGeoLocation(), "null").toString());
                    dataJson.addProperty("UserMentionEntitiesSize", java.util.Objects.toString(status.getUserMentionEntities().length, "null").toString());
                    dataJson.addProperty("Place", java.util.Objects.toString(status.getPlace(), "null").toString());
                    dataJson.addProperty("Lang", java.util.Objects.toString(status.getLang(), "null").toString());
                    dataJson.addProperty("ContributorsSize", java.util.Objects.toString(status.getContributors().length, "null").toString());

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.add("Twitter", dataJson);

                    //send tweet to kafka
                    this.kafka.sendMessage(jsonObject.toString());

                }

                @Override
                public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                    System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
                }

                @Override
                public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                    System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
                }

                @Override
                public void onScrubGeo(long userId, long upToStatusId) {
                    System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
                }

                @Override
                public void onStallWarning(StallWarning warning) {
                    System.out.println("Got stall warning:" + warning);
                }

                @Override
                public void onException(Exception ex) {
                    ex.printStackTrace();
                }
            };


        File file = new File(filePathCred);
        Properties prop = new Properties();
        InputStream is = null;

        //check if twitter config file exists and if yes load to properties variable
        if (file.exists()) {
            try {
                is = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                prop.load(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Twitter auth file not found");
        }

        //set twitter configuration
        ConfigurationBuilder cb = new ConfigurationBuilder();

        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(prop.getProperty("key"))
                .setOAuthConsumerSecret(prop.getProperty("secret"))
                .setOAuthAccessToken(prop.getProperty("action.token"))
                .setOAuthAccessTokenSecret(prop.getProperty("action.secret"));


        //build twitter stream
        Configuration tfConf = cb.build();
        TwitterStream twitterStream = new TwitterStreamFactory(tfConf).getInstance();
        twitterStream.addListener(listener);

        //filter twitter stream to receive tweets with #bitcoin
        FilterQuery tweetFilterQuery = new FilterQuery();
        tweetFilterQuery.track(new String[]{"bitcoin"});

            /*tweetFilterQuery.locations(new double[][]{new double[]{-126.562500,30.448674},
                    new double[]{-61.171875,44.087585
                    }}); //Note that not all tweets have location metadata set.
            */
        //filter twitter stream to receive tweets in english
        tweetFilterQuery.language(new String[]{"en"});

        //start twitter stream with filters
        twitterStream.filter(tweetFilterQuery);

    }
}

