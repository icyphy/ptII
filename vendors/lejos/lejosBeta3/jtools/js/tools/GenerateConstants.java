package js.tools;

import java.util.*;
import java.io.*;

public class GenerateConstants
{
  static String iTinyVMHome = System.getProperty ("tinyvm.home");
  static final String CLASSES = "SpecialClassConstants";
  static final String SIGNATURES = "SpecialSignatureConstants";

  public static void fatal (String aMsg)
  {
    System.err.println (aMsg);
    System.exit(1);
  }

  public static String hclassName (String aClassName)
  {
    return aClassName.replace('/', '_').toUpperCase();
  }

  public static String htransform (String aSig)
  {
    return aSig.replace('[','A').replace(';','0').replace('/','_');
  }

  public static String hsignatureName (String aSignature)
  {
    int pParIdx = aSignature.indexOf ('(');
    String pName = aSignature.substring (0, pParIdx);
    String pNormName = pName.replace('<','_').replace('>','_');
    int pEndIdx = aSignature.lastIndexOf (')');
    String pRetType = aSignature.substring (pEndIdx+1);
    return pNormName.toUpperCase() + "_" + htransform(pRetType);
  }

  public static void generateClassConstants (Vector aVec, File aHeaderFile, File aJavaFile)
  throws Exception
  {
    PrintWriter pHeaderOut = new PrintWriter (new FileWriter (aHeaderFile));    
    PrintWriter pJavaOut = new PrintWriter (new FileWriter (aJavaFile));

    pHeaderOut.println ("/**");
    pHeaderOut.println (" * Machine-generated file. Do not modify.");
    pHeaderOut.println (" */");
    pHeaderOut.println();
    pHeaderOut.println ("#ifndef _SPECIALCLASSES_H");
    pHeaderOut.println ("#define _SPECIALCLASSES_H");

    pJavaOut.println ("package js.tinyvm;");
    pJavaOut.println ("/**");
    pJavaOut.println (" * Machine-generated file. Do not modify.");
    pJavaOut.println (" */");
    pJavaOut.println ("");
    pJavaOut.println ("public interface " + CLASSES + " {");
    pJavaOut.println ("  static final String[] CLASSES = {");
    try {  
      int pSize = aVec.size();
      for (int i = 0; i < pSize; i++)
      {
        String pClassName = (String) aVec.elementAt (i);
        
        pJavaOut.print ("    \"" + pClassName + "\"");
        if (i < pSize - 1)
          pJavaOut.println (",");
        else
          pJavaOut.println();

        pHeaderOut.println ("#define " + hclassName (pClassName) + " " + i);
      }
    } finally {
      pJavaOut.println ("  };");
      pJavaOut.println ("}");
      pJavaOut.close();

      pHeaderOut.println ("#endif _SPECIALCLASSES_H");
      pHeaderOut.close();
    }
  }

  public static void generateSignatureConstants (Vector aVec, File aHeaderFile, File aJavaFile)
  throws Exception
  {
    PrintWriter pHeaderOut = new PrintWriter (new FileWriter (aHeaderFile));    
    PrintWriter pJavaOut = new PrintWriter (new FileWriter (aJavaFile));

    pHeaderOut.println ("/**");
    pHeaderOut.println (" * Machine-generated file. Do not modify.");
    pHeaderOut.println (" */");
    pHeaderOut.println();
    pHeaderOut.println ("#ifndef _SPECIALSIGNATURES_H");
    pHeaderOut.println ("#define _SPECIALSIGNATURES_H");

    pJavaOut.println ("package js.tinyvm;");
    pJavaOut.println ("/**");
    pJavaOut.println (" * Machine-generated file. Do not modify.");
    pJavaOut.println (" */");
    pJavaOut.println ("");
    pJavaOut.println ("public interface " + SIGNATURES + " {");
    pJavaOut.println ("  static final String[] SIGNATURES = {");
    try {  
      int pSize = aVec.size();
      for (int i = 0; i < pSize; i++)
      {
        String pSignature = (String) aVec.elementAt (i);
        
        pJavaOut.print ("    \"" + pSignature + "\"");
        if (i < pSize - 1)
          pJavaOut.println (",");
        else
          pJavaOut.println();

        pHeaderOut.println ("#define " + hsignatureName (pSignature) + " " + i);
      }
    } finally {
      pJavaOut.println ("  };");
      pJavaOut.println ("}");
      pJavaOut.close();

      pHeaderOut.println ("#endif _SPECIALSIGNATURES_H");
      pHeaderOut.close();
    }
  }

  public static void main (String[] aArg)
  throws Exception
  {
    if (iTinyVMHome == null)
      fatal ("Error: Property tinyvm.home undefined");
    File pHome = new File (iTinyVMHome);
    if (!pHome.exists())
      fatal ("Error: " + iTinyVMHome + " does not exist.");
    if (!pHome.isDirectory())
      fatal ("Error: " + iTinyVMHome + " is not a directory.");
    File pCommon = new File (pHome, "common");
    File pClasses = new File (pCommon, "classes.db");
    File pSignatures = new File (pCommon, "signatures.db");
    File pVmSrc = new File (pHome, "vmsrc");
    File pClassIndexH = new File (pVmSrc, "specialclasses.h");
    File pSignaturesH = new File (pVmSrc, "specialsignatures.h");
    File pTools = new File (pHome, "jtools");
    File pToolsJs = new File (pTools, "js");
    File pToolsJsTinyVM = new File (pToolsJs, "tinyvm");
    File pJavaClass = new File (pToolsJsTinyVM, CLASSES + ".java");
    File pJavaSig = new File (pToolsJsTinyVM, SIGNATURES + ".java");

    Vector pClassVec = ListReader.loadStrings (pClasses);
    Vector pSignatureVec = ListReader.loadStrings(pSignatures);

    // Add a fake entry for the main class
    // (Not done since 1.0.0alpha5)
    // pClassVec.insertElementAt ("entry/class", 0);

    generateClassConstants (pClassVec, pClassIndexH, pJavaClass);
    generateSignatureConstants (pSignatureVec, pSignaturesH, pJavaSig);
  }
}
