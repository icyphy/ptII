package ptolemy.lang;

class StringManip {
  public static final String unqualifiedPart(String qualName) {
    int lastIndex = qualName.lastIndexOf('.');

    return qualName.substring(lastIndex + 1);
  }

}