import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class convertTime {
    private String textDate;

    public convertTime(String textDate){
        this.textDate = textDate;
    };

    public String getTextDate() {
        return textDate;
    }

    public void setTextDate(String textDate) {
        this.textDate = textDate;
    }

    public String toUnixTimestamp(){
        Date parsed;
        try {
            SimpleDateFormat format =
                    new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
            parsed = format.parse(textDate);
        }
        catch(ParseException pe) {
            throw new IllegalArgumentException(pe);
        }

        Long longTime = new Long(parsed.getTime()/1000);

        return longTime.toString();
    }

    public String help(){
        return "Example: pass in - 'Thu Mar 22 22:51:21 EDT 2018' and get back '1521773382'";
    }

}
