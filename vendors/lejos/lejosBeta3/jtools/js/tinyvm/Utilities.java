package js.tinyvm;

import java.util.*;

public class Utilities
{
  static int iVerboseLevel;
  static boolean iTrace = Boolean.getBoolean ("trace");

  public static void setVerboseLevel (int aVerboseLevel)
  {
    iVerboseLevel = aVerboseLevel;
  }

  public static int getVerboseLevel()
  {
    return iVerboseLevel;
  }

  public static void assert (boolean aCond)
  {
    if (!aCond)
    {
      System.err.println ("Assertion violation.");
      new Error().printStackTrace();
      System.exit(1);
    }
  }

  public static void fatal (String aMsg)
  {
    System.err.println ("Fatal: " + aMsg);
    System.exit(1);
  }

  public static void verbose (int aLevel, String aMsg)
  {
    if (iVerboseLevel >= aLevel)
      System.out.println (aMsg);
  }

  public static void trace (String aMsg)
  {
    if (iTrace)
      System.out.println ("(" + System.currentTimeMillis() + ") " + aMsg);
  }

  public static boolean isVerbose (int aLevel)
  {
    return iVerboseLevel >= aLevel;
  }
}
