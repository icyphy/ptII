package ptolemy.backtrack.test;

import ptolemy.backtrack.test.ptolemy.backtrack.test.random1.RandomTest1;

public class RandomTest1Main {
    /**
     * @param args
     */
    public static void main(String[] args) {
        RandomTest1 test = new RandomTest1();
        long handle = test.$GET$CHECKPOINT().createCheckpoint();
        int[] buffer1 = test.generate();
        test.$GET$CHECKPOINT().rollback(handle, true);

        int[] buffer2 = test.generate();

        for (int i = 0; i < buffer1.length; i++) {
            System.out.print(buffer1[i] - buffer2[i] + " ");
        }

        System.out.println();
    }
}
