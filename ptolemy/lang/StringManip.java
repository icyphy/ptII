package ptolemy.lang;

import java.io.File;

public class StringManip {

    public static final String unqualifiedPart(String qualifiedName) {
      return partAfterLast(qualifiedName, '.');
    }

    public static final String rawFilename(String filename) {
      return partAfterLast(filename, File.separatorChar);
    }

    /** Return the substring that follows the last occurence of the
     *  argument character in the argument string. If the
     *  character does not occur, return the whole string.
     */
    public static final String partAfterLast(String str, char c) {
        return str.substring(str.lastIndexOf(c) + 1);
    }
    
    /** Return the substring that precedes the last occurence of the
     *  argument character in the argument string. If the
     *  character does not occur, return the whole string.
     */
    public static final String partBeforeLast(String str, char c) {
        return str.substring(0, str.lastIndexOf(c));
    }
    
}