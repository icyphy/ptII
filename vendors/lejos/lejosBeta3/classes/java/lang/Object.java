package java.lang;

/**
 * All classes extend this one, implicitly.
 */
public class Object
{
  public boolean equals (Object aOther)
  {
    return this == aOther;
  }

  public int hashCode()
  {
    return getDataAddress (this);
  }

//  public final native void notify() throws InterruptedException;
//  public final native void notifyAll() throws InterruptedException;
//  public final native void wait() throws InterruptedException;
  
  /**
   * Returns the empty string. It's here to satisfy javac.
   */
  public String toString()
  {
    return "";
  }

  /**
   * Returns <code>null</code>. It's here to satisfy javac.
   */
  public final Class getClass()
  {
    return null;
  }

  private native static int getDataAddress (Object obj);
}







