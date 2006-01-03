/* Copyright (c) 2005 Sun Microsystems
 */
package doc.doclets;

import com.sun.javadoc.*;

/** Print the name of the class to standard out.
 *  This code is from
 *  <a href="http://java.sun.com/j2se/1.5.0/docs/guide/javadoc/doclet/overview.html"><code>http://java.sun.com/j2se/1.5.0/docs/guide/javadoc/doclet/overview.html</code></a>.
 *  This class should be removed once we have our own code in place.
 */
public class ListClass {
    public static boolean start(RootDoc root) {
        ClassDoc[] classes = root.classes();
        for (int i = 0; i < classes.length; ++i) {
            System.out.println(classes[i]);
        }
        return true;
    }
}
