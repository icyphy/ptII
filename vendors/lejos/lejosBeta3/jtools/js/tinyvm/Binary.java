package js.tinyvm;

import java.io.*;
import java.util.*;
import js.classfile.*;

/**
 * Abstraction for dumped binary.
 */
public class Binary implements SpecialClassConstants, SpecialSignatureConstants
{
  // State that is written to the binary:
  final RecordTable iEntireBinary = new Sequence();

  // Contents of binary:
  final MasterRecord iMasterRecord = new MasterRecord (this);
  final EnumerableSet iClassTable = new EnumerableSet();
  final EnumerableSet iConstantTable = new EnumerableSet();
  final RecordTable iEntryClassIndices = new Sequence();
  final RecordTable iMethodTables = new Sequence();
  final RecordTable iInstanceFieldTables = new Sequence();
  final RecordTable iStaticFields = new Sequence();
  final RecordTable iExceptionTables = new EnumerableSet();
  final RecordTable iStaticState = new Sequence (true);
  final RecordTable iCodeSequences = new Sequence();
  final RecordTable iConstantValues = new Sequence();

  // Other state:
  final Hashtable iSpecialSignatures = new Hashtable();
  final Hashtable iClasses = new Hashtable();
  final HashVector iSignatures = new HashVector();

  public void dump (ByteWriter aOut) throws Exception
  {
    iEntireBinary.dump (aOut);
  }

  public boolean hasMain (String aClassName)
  {
    ClassRecord pRec = getClassRecord (aClassName);
    return pRec.hasMethod (new Signature ("main", "([Ljava/lang/String;)V"), true);  	 
  }
  
  public ClassRecord getClassRecord (String aClassName)
  {
    return (ClassRecord) iClasses.get (aClassName);
  }

  public int getClassIndex (String aClassName)
  {
    return getClassIndex (getClassRecord (aClassName));
  }

  public int getClassIndex (ClassRecord aRec)
  {
    if (aRec == null)
      return -1;
    return iClassTable.indexOf (aRec);
  }

  public int getConstantIndex (ConstantRecord aRec)
  {
    if (aRec == null)
      return -1;
    return iConstantTable.indexOf (aRec);
  }

  public ConstantRecord getConstantRecord (int aIndex)
  {
    return (ConstantRecord) iConstantTable.elementAt (aIndex);
  }

  public void processClasses (Vector aEntryClasses, ClassPath aClassPath)
  throws Exception
  {
    // Add special classes first
    for (int i = 0; i < CLASSES.length; i++)
    {
      String pName = CLASSES[i];
      ClassRecord pRec = ClassRecord.getClassRecord (pName, aClassPath, this);
      iClasses.put (pName, pRec);
      iClassTable.add (pRec);
    }
    // Now add entry classes
    int pEntrySize = aEntryClasses.size();
    for (int i = 0; i < pEntrySize; i++)
    {
      String pName = (String) aEntryClasses.elementAt (i);
      ClassRecord pRec = ClassRecord.getClassRecord (pName, aClassPath, this);
      iClasses.put (pName, pRec);
      iClassTable.add (pRec);
      // Update table of indices to entry classes
      iEntryClassIndices.add (new EntryClassIndex (this, pName));
    }      
    Utilities.trace ("Starting with " + iClassTable.size() + " classes.");
    // Now add the closure.
    // Yes, call iClassTable.size() in every pass of the loop.
    for (int pIndex = 0; pIndex < iClassTable.size(); pIndex++)
    {
      ClassRecord pRec = (ClassRecord) iClassTable.elementAt(pIndex);
      Utilities.verbose (1, "Class " + pIndex + ": " + pRec.iName);
      pRec.storeReferredClasses (iClasses, iClassTable, aClassPath);
    }
    // Initialize indices and flags
    int pSize = iClassTable.size();
    for (int pIndex = 0; pIndex < pSize; pIndex++)
    {
      ClassRecord pRec = (ClassRecord) iClassTable.elementAt(pIndex);
      pRec.iIndex = pIndex;
      pRec.initFlags();
      pRec.initParent();
    }   
  }

  public void processSpecialSignatures()
  {
    for (int i = 0; i < SIGNATURES.length; i++)
    {
      Signature pSig = new Signature (SIGNATURES[i]);
      iSignatures.addElement (pSig);
      iSpecialSignatures.put (pSig, SIGNATURES[i]);
    }
  }

  public boolean isSpecialSignature (Signature aSig)
  {
    return iSpecialSignatures.containsKey (aSig);
  }

  public void processConstants()
  {
    int pSize = iClassTable.size();
    for (int pIndex = 0; pIndex < pSize; pIndex++)
    {
      ClassRecord pRec = (ClassRecord) iClassTable.elementAt(pIndex);
      pRec.storeConstants (iConstantTable, iConstantValues);
    }    
  }

  /**
   * Calls storeMethods on all the classes of the closure
   * previously computed with processClasses.
   */
  public void processMethods()
  {
    int pSize = iClassTable.size();
    for (int pIndex = 0; pIndex < pSize; pIndex++)
    {
      ClassRecord pRec = (ClassRecord) iClassTable.elementAt(pIndex);
      pRec.storeMethods (iMethodTables, iExceptionTables,
                         iSignatures);
    }        
  }

  public void processFields()
  {
    int pSize = iClassTable.size();
    for (int pIndex = 0; pIndex < pSize; pIndex++)
    {
      ClassRecord pRec = (ClassRecord) iClassTable.elementAt(pIndex);
      pRec.storeFields (iInstanceFieldTables, iStaticFields, iStaticState);
    }        
  }

  public void processCode (boolean aPostProcess)
  {
    int pSize = iClassTable.size();
    for (int pIndex = 0; pIndex < pSize; pIndex++)
    {
      ClassRecord pRec = (ClassRecord) iClassTable.elementAt(pIndex);
      pRec.storeCode (iCodeSequences, aPostProcess);
    }        
  }

  public void storeComponents()
  {
    // Master record and class table are always the first two:
    iEntireBinary.add (iMasterRecord);
    iEntireBinary.add (iClassTable);
    // 5 aligned components:
    iEntireBinary.add (iStaticState);
    iEntireBinary.add (iStaticFields);
    iEntireBinary.add (iConstantTable);
    iEntireBinary.add (iMethodTables);
    iEntireBinary.add (iExceptionTables);
    // 4 unaligned components:
    iEntireBinary.add (iInstanceFieldTables);
    iEntireBinary.add (iCodeSequences);
    iEntireBinary.add (iConstantValues);
    iEntireBinary.add (iEntryClassIndices);
  }

  public void initOffsets()
  {
    iEntireBinary.initOffset (0);
  }

  public int getTotalNumMethods()
  {
    int pTotal = 0;
    int pSize = iMethodTables.size();
    for (int i = 0; i < pSize; i++)
    {
      pTotal += ((RecordTable) iMethodTables.elementAt(i)).size();
    }    
    return pTotal;
  }

  public int getTotalNumInstanceFields()
  {
    int pTotal = 0;
    int pSize = iInstanceFieldTables.size();
    for (int i = 0; i < pSize; i++)
    {
      pTotal += ((RecordTable) iInstanceFieldTables.elementAt(i)).size();
    }    
    return pTotal;
  }

  public void report()
  {
    if (Utilities.getVerboseLevel() == 0)
      return;
    int pSize = iSignatures.size();
    for (int i = 0; i < pSize; i++)
    {
      Signature pSig = (Signature) iSignatures.elementAt (i);
      System.out.println ("Signature " + i + ": " + pSig.getImage());
    }
    if (Utilities.getVerboseLevel() <= 1)
      return;
    System.out.println ("Master record : " + iMasterRecord.getLength() + 
                        " bytes.");
    System.out.println ("Class records : " + iClassTable.size() + " (" +
                        iClassTable.getLength() + " bytes).");
    System.out.println ("Field records : " + getTotalNumInstanceFields() + 
                        " (" + iInstanceFieldTables.getLength() + " bytes).");
    System.out.println ("Method records: " + getTotalNumMethods() + " (" +
                        iMethodTables.getLength() + " bytes).");
    System.out.println ("Code          : " + iCodeSequences.size() + " (" +
                        iCodeSequences.getLength() + " bytes).");

    Utilities.verbose (2, "Class table offset   : " + iClassTable.getOffset());
    Utilities.verbose (2, "Constant table offset: " + iConstantTable.getOffset());
    Utilities.verbose (2, "Method tables offset : " + iMethodTables.getOffset());
    Utilities.verbose (2, "Excep tables offset  : " + iExceptionTables.getOffset());
  }
		       
  public static Binary createFromClosureOf (Vector aEntryClasses, ClassPath aClassPath)
  throws Exception
  {
    Binary pBin = new Binary();
    // From special classes and entry class, store closure
    pBin.processClasses (aEntryClasses, aClassPath);
    // Store special signatures
    pBin.processSpecialSignatures();
    pBin.processConstants();
    pBin.processMethods();
    pBin.processFields();
    // Copy code as is (first pass)
    pBin.processCode (false);
    pBin.storeComponents();
    pBin.initOffsets();
    // Post-process code after offsets are set (second pass)
    pBin.processCode (true);
    // Do -verbose reporting
    pBin.report();
    return pBin;
  }
}







