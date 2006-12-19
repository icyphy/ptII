package ptolemy.actor.ptalon.demo;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;

public class ReverseLink extends MapReduceAlgorithm {

    public List<KeyValuePair> map(String key, String value) {
        StringTokenizer tokenizer = new StringTokenizer(value);
        LinkedList<KeyValuePair> output = new LinkedList<KeyValuePair>();
        while (tokenizer.hasMoreTokens()) {
            output.add(new KeyValuePair(tokenizer.nextToken(), key));
        }
        return output;
    }

    public List<String> reduce(String key, BlockingQueue<String> values)
            throws InterruptedException {
        List<String> output = new LinkedList<String>();
        while (!isQueueEmpty()) {
            String value = values.take();
            if (isQueueEmpty()) {
                break;
            }
            output.add(value);
        }
        return output;
    }

}
