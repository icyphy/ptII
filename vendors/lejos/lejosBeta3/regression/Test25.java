import tinyvm.rcx.*;

public class Test25
{
  public static void myMethod (Object array)
  {
    if (array instanceof byte[])
    {
      byte[] ba = (byte[]) array;
      Sound.playTone (ba[0] * 10, 100);
    }
  }

  public static void main (String[] arg) 
  {
    myMethod (new byte[] { 100 } );
    for (int k = 0; k < 50000; k++) { }
  }
}



