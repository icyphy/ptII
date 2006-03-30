/** Class that creates lots of ComponentEntities */
package ptolemy.kernel.test;

import ptolemy.kernel.ComponentEntity;

public class ComponentEntityTimeTest {
    public static void main(String args[]) {
        ComponentEntity entities[] = new ComponentEntity[10000];
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 10000; i++) {
            entities[i] = new ComponentEntity();
        }
        long stopTime = System.currentTimeMillis();
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / 1024;
        long freeMemory = runtime.freeMemory() / 1024;
        System.out
                .println(stopTime
                        - startTime
                        + " ms. Memory: "
                        + totalMemory
                        + " K Free: "
                        + freeMemory
                        + " K ("
                        + Math
                                .round((((double) freeMemory) / ((double) totalMemory)) * 100.0)
                        + "%)");

    }
}
