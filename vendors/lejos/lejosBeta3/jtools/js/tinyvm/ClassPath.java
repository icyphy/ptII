package js.tinyvm;

import java.io.*;
import java.util.zip.*;
import java.util.*;

public class ClassPath
{
  private Object[] iEntries;

  public ClassPath (String aEntries)
  throws Exception
  {
    Vector pTokens = new Vector();
    StringTokenizer pTok = new StringTokenizer (aEntries, File.pathSeparator); 
    while (pTok.hasMoreTokens())
    {
      pTokens.addElement (pTok.nextToken());
    }
    iEntries = new Object[pTokens.size()];
    for (int i = 0; i < iEntries.length; i++)
    {
      String pEntry = (String) pTokens.elementAt(i);
      if (pEntry.endsWith (".zip") || pEntry.endsWith (".jar"))
      {
	try {
          iEntries[i] = new ZipFile (pEntry);
	} catch (ZipException e) {
          System.err.println ("Warning: Can't open zip/jar file: " + pEntry);
	}
      }
      else
      {
        iEntries[i] = new File (pEntry);
        if (!((File) iEntries[i]).isDirectory())
          Utilities.fatal (pEntry + " is not a directory.");
      }
    }
  }

  /**
   * @param aName Fully qualified class name with '/' characters.
   * @return <code>null</code> iff not found.
   */
  public InputStream getInputStream (String aName)
  throws Exception
  {
    String pRelName = aName + ".class";
    for (int i = 0; i < iEntries.length; i++)
    {
      if (iEntries[i] instanceof File)
      {
        File pClassFile = new File ((File) iEntries[i], pRelName);
        if (pClassFile.exists())
          return new FileInputStream (pClassFile);
      }
      else if (iEntries[i] instanceof ZipFile)
      {
        ZipFile pZipFile = (ZipFile) iEntries[i];
        ZipEntry pEntry = pZipFile.getEntry (pRelName);
        if (pEntry != null)
          return pZipFile.getInputStream (pEntry);
      }
    }
    return null;
  }

  public String toString()
  {
    StringBuffer pBuf = new StringBuffer();
    for (int i = 0; i < iEntries.length; i++)
    {
      if (iEntries[i] instanceof ZipFile)
        pBuf.append (((ZipFile) iEntries[i]).getName());
      else
        pBuf.append (iEntries[i]);
      if (i < iEntries.length - 1)
        pBuf.append (File.pathSeparator);
    }
    return pBuf.toString();
  }
}




