package ptolemy.actor.ptalon.demo;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;

public class WordCount extends MapReduceAlgorithm {

    public List<KeyValuePair> map(String key, String value) {
        StringTokenizer tokenizer = new StringTokenizer(value);
        LinkedList<KeyValuePair> output = new LinkedList<KeyValuePair>();
        while (tokenizer.hasMoreTokens()) {
            output.add(new KeyValuePair(tokenizer.nextToken(), "1"));
        }
        return output;
    }

    public List<String> reduce(String key, BlockingQueue<String> values)
            throws InterruptedException {
        int result = 0;
        while (!isQueueEmpty()) {
            String value = values.take();
            if (isQueueEmpty()) {
                break;
            }
            result += Integer.parseInt(value);
        }
        List<String> output = new LinkedList<String>();
        output.add((new Integer(result)).toString());
        return output;
    }

}
