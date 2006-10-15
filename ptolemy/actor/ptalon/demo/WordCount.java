package ptolemy.actor.ptalon.demo;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class WordCount extends MapReduceAlgorithm {

    public List<KeyValuePair> map(String key, String value) {
        StringTokenizer tokenizer = new StringTokenizer(value);
        LinkedList<KeyValuePair> output = new LinkedList<KeyValuePair>();
        while (tokenizer.hasMoreTokens()) {
            output.add(new KeyValuePair(tokenizer.nextToken(), "1"));
        }
        return output;
    }
    
    public List<String> reduce(String key, List<String> value) {
        return null;
    }
    
}
