package java.lang;

/**
 * An expandable string of characters.
 */
public class StringBuffer
{
  char[] characters;
  int size;

  public StringBuffer (String aString)
  {
    characters = aString.toCharArray();
  }

  public StringBuffer append (String s)
  {
    // Reminder: compact code more important than speed
    char[] sc = s.toCharArray();
    int cl = characters.length;
    int sl = sc.length;
    char[] nc = new char[sl + cl];
    System.arraycopy (characters, 0, nc, 0, cl);
    System.arraycopy (sc, 0, nc, cl, sl);
    characters = nc;
    return this;
  }

  public StringBuffer append (java.lang.Object aObject)
  {
    return append (aObject.toString());
  }

  public StringBuffer append (boolean aBoolean)
  {
    return append (aBoolean ? "true" : "false");
  }
  
  public StringBuffer append (char aChar)
  {
    return append (new String (new char[] { aChar }, 0, 1));
  }

  public StringBuffer append (int aInt)
  {
    return append ("<no str+int>");
  }

  public StringBuffer append (long aLong)
  {
    return append ("<no str+long>");
  }

  public StringBuffer append (float aFloat)
  {
    return append ("<no str+float>");
  }

  public StringBuffer append (double aDouble)
  {
    return append ("<no str+double>");
  }
  
  public String toString()
  {
    return new String (characters, 0, characters.length);
  }
}


