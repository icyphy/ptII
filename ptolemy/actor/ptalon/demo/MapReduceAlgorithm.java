package ptolemy.actor.ptalon.demo;

import java.util.List;

public abstract class MapReduceAlgorithm extends Thread {
    
    public abstract List<KeyValuePair> map(String key, String value);
    
    public abstract List<String> reduce(String key, List<String> value);
}
