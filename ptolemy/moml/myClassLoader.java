package ptolemy.moml;

import java.lang.ClassLoader;
import java.io.IOException;


/**
 * a class extends the ClassLoader, so that the protected defineClass() method
 * can be called.
 *
 * @author Yang Zhao
 *
 */
public class MyClassLoader extends ClassLoader {
    public Class myDefineClass (String name, byte[] b, int off, int len) {
        Class myClass = null;
        try {
          // try to turn them into a class
            myClass = defineClass( name, b, 0, len);
        } catch( java.lang.ClassFormatError e ) {
          // This is not a failure!  If we reach here, it might
          // mean that we are dealing with a class in a library,
          // such as java.lang.Object
        }
        return myClass;
    }

     public void myResolveClass (Class c) {
         resolveClass( c );
     }

}
