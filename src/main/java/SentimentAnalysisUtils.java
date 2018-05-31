/*
Pulled from follwoing gitHub:
https://github.com/t3g7/spark-facebook/blob/master/src/main/java/com/t3g7/spark/facebook/SentimentAnalysisUtils.java
 */

import edu.stanford.nlp.international.french.process.FrenchTokenizer;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Set;

public class SentimentAnalysisUtils {
    private SentimentAnalysisUtils() {
    }

    private static SentimentAnalysisUtils INSTANCE = new SentimentAnalysisUtils();

    public static SentimentAnalysisUtils getInstance() {
        return INSTANCE;
    }

    TokenizerFactory<CoreLabel> tokenizerFactory = FrenchTokenizer.ftbFactory();
    Utils utils = Utils.getInstance();

    public ArrayList<String> tokenize(String content, Set<String> stopWords) {
        //System.out.println(content);
        StringReader stringReader = new StringReader(content);
        Tokenizer<CoreLabel> tokenizer = tokenizerFactory
                .getTokenizer(stringReader);

        // is ArrayList the true translation of the ArrayBuffer ?
        ArrayList<String> tokens = new ArrayList<String>();

        while (tokenizer.hasNext()) {
            String token = tokenizer.next().toString();
            if (!stopWords.contains(token.toLowerCase()) && token.length() > 1) {
                tokens.add(token);
            }
        }
        //System.out.println("DEBUG - tokens: " + tokens);
        return tokens;
    }

    public int countWeight(ArrayList<String> tokens, Set<String> wordSet) {
        int weight = 0;

        for (String token : tokens) {
            if (wordSet.contains(token)) {
                weight++;
            }
        }
        return weight;
    }

    public float detectSentiment (String text) {
        if (text == null ) {
            // For posts that contain no text such as sharing
           // return SentimentType.NEUTRAL;
            return 0.001f;
        }

        Set<String> posWordsSet = utils.getLinesSet("/wordsets/pos-words.txt");
        Set<String> negWordsSet = utils.getLinesSet("/wordsets/neg-words.txt");
        Set<String> stopWordsSet = utils.getLinesSet("/wordsets/stop-words.txt");

        ArrayList <String> tokens = tokenize(text, stopWordsSet);

        int posWordsWeight = countWeight(tokens, posWordsSet);
        int negWordsWeight = countWeight(tokens, negWordsSet);

        //System.out.println("DEBUG - pos weight: " + posWordsWeight);
        //System.out.println("DEBUG - neg weight: " + negWordsWeight);

        int sentiment = posWordsWeight - negWordsWeight;
        //System.out.println("DEBUG - sentiment score = " + sentiment);
        /*
        if (sentiment > 0)
            return SentimentType.POSITIVE;
        else if (sentiment < 0)
            return SentimentType.NEGATIVE;
        else
            return SentimentType.NEUTRAL;
        */
        return sentiment;
    }

}
