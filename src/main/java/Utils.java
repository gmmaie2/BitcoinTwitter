/*
Pulled from follwoing gitHub:
https://github.com/t3g7/spark-facebook/blob/master/src/main/java/com/t3g7/spark/facebook/Utils.java
*/

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Utils {

    private Utils() {
    }

    private static Utils INSTANCE = new Utils();

    public static Utils getInstance() {
        return INSTANCE;
    }

    Long lastTimestamp = System.currentTimeMillis() / 1000 - 100000;

    public void setLastTimestamp() {
        lastTimestamp = System.currentTimeMillis() / 1000 - 100000;
    }

    public ArrayList<String> getLines(String resource) {
        InputStreamReader isr = new InputStreamReader(getClass()
                .getResourceAsStream(resource));
        Scanner s = new Scanner(isr);

        ArrayList<String> lines = new ArrayList<String>();
        while (s.hasNext()) {
            lines.add(s.next());
        }
        s.close();

        return lines;
    }

    public String getLine(String resource) {
        InputStreamReader isr = new InputStreamReader(getClass()
                .getResourceAsStream(resource));
        Scanner s = new Scanner(isr);

        String line = s.next();
        s.close();

        return line;
    }

    public Set<String> getLinesSet(String resource) {
        InputStreamReader isr = new InputStreamReader(getClass()
                .getResourceAsStream(resource));
        Scanner s = new Scanner(isr);

        Set<String> lines = new HashSet<String>();

        while (s.hasNext()) {
            lines.add(s.next());
        }

        s.close();

        return lines;
    }
}