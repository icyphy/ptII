// JNI Example for gcc from http://www.inonit.com/cygwin/jni/helloWorld/java.html
package jni.demo.HelloWorld;

public class HelloWorld {
    private static native void writeHelloWorldToStdout();

    // private static void writeHelloWorldToStdout() {
    //    System.out.println("Hello World");
    //}
    
    public static void main(String[] args) {
	System.loadLibrary("HelloWorld");
        writeHelloWorldToStdout();
    }
}
