package java.lang;

/**
 * System utilities.
 */
public class System
{
  private System() {}
  
  /**
   * Copies one array to another.
   */
  //public static native void arraycopy (Object src, int srcoffset, Object dest, int destoffset, int length);
  static void arraycopy (char[] src, int srcoffset, char[] dest, int destoffset, int length)
  {
    for (int i = 0; i < length; i++)
      dest[i + destoffset] = src[i + srcoffset]; 
  }
  
  /**
   * Current time expressed in milliseconds. In the RCX, this is the number
   * of milliseconds since the RCX has been on. (In Java, this would
   * be since January 1st, 1970).
   */
  public static native long currentTimeMillis();
}

