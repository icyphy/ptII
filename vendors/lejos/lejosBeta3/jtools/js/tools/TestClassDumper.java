package js.tools;

import java.io.*;
import js.classfile.*;

public class TestClassDumper
{
  public static void test (String aFileName)
  throws Exception
  {
    System.out.println ("Checking " + aFileName + " ...");
    FileInputStream pIn = new FileInputStream (aFileName);
    JClassFile pClassFile = new JClassFile();
    pClassFile.read (pIn);
    pIn.close();
    File pBakFile = new File (aFileName + "~");
    pBakFile.delete();
    new File (aFileName).renameTo (pBakFile);
    FileOutputStream pOut = new FileOutputStream (aFileName);
    pClassFile.dump (pOut);
    pOut.close();
  }

  public static void main (String[] args)
  throws Exception
  {  
    for (int i = 0; i < args.length; i++)
      test (args[i]);
  }
}
