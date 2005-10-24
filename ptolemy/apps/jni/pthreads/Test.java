// Test.java
package ptolemy.apps.jni.pthreads;

class Test {
    long c = 0;

    public native void test_start_callback();

    public native void test_stop_callback();

    static {
        try {
            System.out.println("Loading test");
            System.loadLibrary("test");
            System.out.println("Done loading test");
        } catch (Throwable throwable) {
            System.err.println("System.loadLibrary (): " + throwable);
            System.exit(1);
        }
    }

    public void start() {
        test_start_callback();
    }

    public void stop() {
        test_stop_callback();
    }

    public void callback() {
        System.out.println("public void callback (): " + c++);
    }
}

class Main {
    public static void main(String[] args) {
        Test test = new Test();

        System.out.println("before start");
        test.start();
        System.out.println("after start");

        try {
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        test.stop();
    }
}
