package java.lang;

/**
 * Not functional. It's here to satisfy javac and jikes.
 */
public class Class
{
  /**
   * @exception java.lang.ClassNotFoundException Thrown always in TinyVM.
   */
  public static Class forName (String aName)
  throws ClassNotFoundException
  {
    throw new ClassNotFoundException();
  }
}
