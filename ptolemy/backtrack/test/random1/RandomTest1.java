package ptolemy.backtrack.test.random1;

import java.util.Random;

public class RandomTest1 {
    
    public int[] generate() {
        int[] buffer = new int[SIZE];
        for (int i = 0; i < SIZE; i++)
            buffer[i] = _random.nextInt();
        return buffer;
    }
        
    public static final int SIZE = 20;
    
    private Random _random = new Random();
    
}
