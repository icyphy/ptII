package ptolemy.lang;

import java.io.File;

public class StringManip {

  public static final String unqualifiedPart(String qualifiedName) {
    return partAfterLast(qualifiedName, '.');
  }

  public static final String rawFilename(String filename) {
    return partAfterLast(filename, File.separatorChar);
  }

  public static final String partAfterLast(String str, char c) {
    int lastIndex = str.lastIndexOf('.');

    return str.substring(lastIndex + 1);
  }

}