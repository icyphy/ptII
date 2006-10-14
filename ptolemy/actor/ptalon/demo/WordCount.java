package ptolemy.actor.ptalon.demo;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class WordCount {

    public static List<KeyValuePair> map(String key, String value) {
        StringTokenizer tokenizer = new StringTokenizer(value);
        LinkedList<KeyValuePair> output = new LinkedList<KeyValuePair>();
        while (tokenizer.hasMoreTokens()) {
            output.add(new KeyValuePair("1", tokenizer.nextToken()));
        }
        return output;
    }
    
}
