package js.test;
import java.lang.reflect.*;

public class TListClasses
{
  public static void main (String aArg[])
  throws Exception
  {
    if (aArg.length != 1)
      throw new RuntimeException ("use: TListClasses <classname>");
    Class pClass = Class.forName (aArg[0]);
    Class[] pDecClasses = pClass.getDeclaredClasses();
    for (int i = 0; i < pDecClasses.length; i++)
      System.out.println (pDecClasses[i].getName());    
  }
}
