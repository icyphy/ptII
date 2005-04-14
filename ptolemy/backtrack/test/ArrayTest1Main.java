package ptolemy.backtrack.test;

import ptolemy.backtrack.test.ptolemy.backtrack.test.array1.ArrayTest1;

public class ArrayTest1Main {

    /**
     * @param args
     */
    public static void main(String[] args) {
        ArrayTest1 test = new ArrayTest1();
        test.$GET$CHECKPOINT().createCheckpoint();
        test.modify();
        test.$GET$CHECKPOINT().rollback(1, true);
        test.dump();
    }

}
