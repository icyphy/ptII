package js.tinyvm;

import java.io.*;
import java.util.*;
import js.classfile.*;

public class TinyVM
implements Constants
{
  static final String CP_PROPERTY = "tinyvm.class.path";
  static final String WO_PROPERTY = "tinyvm.write.order";
  static final String TINYVM_HOME = System.getProperty ("tinyvm.home");
  static final String TINYVM_LOADER = System.getProperty ("tinyvm.loader");
  static final String TEMP_FILE = "__tinyvm__temp.tvm__";
  static String iClassPath = System.getProperty (CP_PROPERTY);
  static String iWriteOrder = System.getProperty (WO_PROPERTY);
  static String iOutputFile;
  static boolean iDoDownload = false;
  static boolean iDumpFile = false;

  private static class Option
  {
    String iOption;
    String iArgument;

    public String toString()
    {
      return iOption + " " + iArgument;
    }
  }

  public static void invokeTvm (String aFileName)
  {
    Utilities.assert (TINYVM_HOME != null);
    Utilities.assert (TINYVM_LOADER != null);
    String pTvmExec = TINYVM_HOME + File.separator + "bin" +
                      File.separator + TINYVM_LOADER; 
    String[] pParams = new String[] { pTvmExec, aFileName };
    try {
      Utilities.verbose (1, "Executing " + pTvmExec + " (downloading) ...");
      Process p = Runtime.getRuntime().exec (pParams);
      pipeStream (p.getInputStream(), System.out);
      pipeStream (p.getErrorStream(), System.err);
      int pStatus;
      if ((pStatus = p.waitFor()) != 0)
      {
        System.err.println (TINYVM_LOADER + ": returned status " + pStatus + ".");
      }
      // Hack: Small wait to get all the output flushed.
      Thread.sleep (100);
      System.out.flush();
      System.err.flush();      
    } catch (InterruptedException e) {
      Utilities.fatal ("Execution of " + pTvmExec + " was interrupted.");
    } catch (IOException e) {
      Utilities.fatal ("Problem executing " + pTvmExec + ". " +
                       "Apparently, the program was not found. ");
    }
  }

  static void pipeStream (final InputStream aIn, final OutputStream aOut)
  {
    Thread pThread = new Thread ("output-pipe")
    {
      public void run()
      {
        try {
          int c;
          for (;;)
	  {
            c = aIn.read();
            if (c == -1)
              Thread.sleep (1);
            else
              aOut.write (c);
	  }
	} catch (Exception e) {
          e.printStackTrace();
	}
      }
    };

    pThread.setDaemon (true);
    pThread.start();
  }

  static void main (String aClassList)
  throws Exception
  {
    Vector pVec = new Vector();
    StringTokenizer pTok = new StringTokenizer (aClassList, ",");
    while (pTok.hasMoreTokens())
    {
      String pClassName = pTok.nextToken();
      pVec.addElement (pClassName.replace ('.', '/').trim());
    }
    main (pVec);
  }
  
  static void main (Vector aEntryClasses)
  throws Exception
  {
    if (aEntryClasses.size() >= 256)
      Utilities.fatal ("Too many entry classes (max is 255!)");
    ClassPath pCP = new ClassPath (iClassPath);
    Binary pBin = Binary.createFromClosureOf (aEntryClasses, pCP);
    int pNum = aEntryClasses.size();
    for (int i = 0; i < pNum; i++)
    {
      String pName = (String) aEntryClasses.elementAt (i);
      if (!pBin.hasMain (pName))
        Utilities.fatal ("Class " + pName + " doesn't have a " +
                         "static void main(String[]) method");
    }
    OutputStream pOut =
      new BufferedOutputStream (new FileOutputStream (iOutputFile), 4096);
    ByteWriter pBW = null;
    if ("BE".equals (iWriteOrder))
      pBW = new BEDataOutputStream (pOut);
    else if ("LE".equals (iWriteOrder))
      pBW = new LEDataOutputStream (pOut);
    else
      Utilities.fatal (WO_PROPERTY + " not BE or LE.");
    pBin.dump (pBW);
    pOut.close();
    if (iDoDownload)
    {
      invokeTvm (TEMP_FILE);
      new File (TEMP_FILE).delete();
    }
  }

  public static void processOptions (Vector aOptions)
  {
    int pSize = aOptions.size();
    for (int i = 0; i < pSize; i++)
    {
      Option pOpt = (Option) aOptions.elementAt(i);
      Utilities.trace ("Option " + i + ": " + pOpt);
      if (pOpt.iOption.equals ("-classpath"))
      {
        iClassPath =  pOpt.iArgument;
      }
      if (pOpt.iOption.equals ("-o"))
      {
        if (iDoDownload)
          Utilities.fatal ("You cannot specify both -d and -o options.");
        iDumpFile = true;
        iOutputFile = pOpt.iArgument;
      }
      else if (pOpt.iOption.equals ("-verbose"))
      {
        int pLevel = 1;
        try {
          pLevel = Integer.parseInt (pOpt.iArgument);
	} catch (Exception e) {
          if (Utilities.iTrace)
            e.printStackTrace();
	}
        Utilities.setVerboseLevel (pLevel);
      }     
    }
    if (!iDumpFile)
    {
      iDoDownload = true;
      iOutputFile = TEMP_FILE;
    }
  }

  public static void main (Vector aArgs, Vector aOptions)
  throws Exception
  {
    if (aArgs.size() != 1)
    {
      System.out.println (TOOL_NAME + " links and downloads a program.");
      System.out.println ("Use: " + TOOL_NAME + " [options] class1[,class2,...] [arg1 arg2 ...]");
      System.out.println ("Options:");
      System.out.println ("  -o <path>         Dump binary into path (no download)");
      System.out.println ("  -verbose[=<n>]    Print class and signature information");
      System.exit (1);
    }
    processOptions (aOptions);
    if (iClassPath == null)
    {
      Utilities.fatal ("Internal error: Classpath not defined. " +
        "Use either -classpath or property " + CP_PROPERTY);
    }
    if (iOutputFile == null)
    {
      Utilities.fatal ("No output file specified. Use -d or -o.");
    }
    main ((String) aArgs.elementAt (0)); 
  }

  public static void main (String[] arg)
  throws Exception
  {
    Vector pRealArgs = new Vector();
    Vector pOptions = new Vector();
    for (int i = 0; i < arg.length; i++)
    {
      if (arg[i].startsWith ("-"))
      {
        Option pOption = new Option();
        pOption.iOption = arg[i];
        if (arg[i].startsWith ("-verbose="))
	{
          pOption.iOption = "-verbose";
          pOption.iArgument = arg[i].substring ("-verbose=".length());
	}
        else if (arg[i].equals ("-classpath"))
	{
          pOption.iArgument = arg[++i];
          Utilities.trace ("Got -classpath option: " + pOption.iArgument);
	}
        else if (arg[i].equals ("-o"))
	{
          pOption.iArgument = arg[++i];
          Utilities.trace ("Got -o option: " + pOption.iArgument);
	}
        else if (arg[i].equals ("-d"))
	{
          pOption.iOption = "-d";
        }
        pOptions.addElement (pOption);
      }
      else
        pRealArgs.addElement (arg[i]);
    }
    main (pRealArgs, pOptions);
  }
}
