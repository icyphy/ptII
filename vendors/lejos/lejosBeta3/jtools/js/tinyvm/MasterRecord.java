package js.tinyvm;

import java.io.*;
import java.util.*;

public class MasterRecord implements WritableData, Constants
{
  Binary iBinary;

  public MasterRecord (Binary aBinary)
  {
    iBinary = aBinary;
  }

  public void dump (ByteWriter aOut) throws Exception
  {
    int pMagicNumber = MAGIC_MASK;
    int pConstantTableOffset = iBinary.iConstantTable.getOffset();
    if (pConstantTableOffset <= 0 || pConstantTableOffset > 0xFFFF)
    {
      Utilities.fatal ("Bug MR-1: Offset=" + pConstantTableOffset + 
                       " CTSize=" + iBinary.iConstantTable.size());
    }
    int pStaticFieldsOffset = iBinary.iStaticFields.getOffset();
    Utilities.assert (pStaticFieldsOffset >= 0 && pStaticFieldsOffset <= 0xFFFF);
    int pStaticStateOffset = iBinary.iStaticState.getOffset();
    Utilities.assert (pStaticStateOffset >= 0 && pStaticStateOffset <= 0xFFFF);
    int pStaticStateLength = (iBinary.iStaticState.getLength() + 1) / 2;
    Utilities.assert (pStaticStateLength >= 0 && pStaticStateLength <= 0xFFFF);
    int pNumStaticFields = iBinary.iStaticFields.size();
    int pEntryClassesOffset = iBinary.iEntryClassIndices.getOffset();
    int pNumEntryClasses = iBinary.iEntryClassIndices.size();
    Utilities.assert (pNumEntryClasses < MAX_CLASSES);
    int pLastClass = iBinary.iClassTable.size() - 1;
    Utilities.assert (pLastClass >= 0 && pLastClass < MAX_CLASSES);
    
    aOut.writeU2 (pMagicNumber);    
    aOut.writeU2 (pConstantTableOffset);
    aOut.writeU2 (pStaticFieldsOffset);
    aOut.writeU2 (pStaticStateOffset);
    aOut.writeU2 (pStaticStateLength);
    aOut.writeU2 (pNumStaticFields);
    aOut.writeU2 (pEntryClassesOffset);
    aOut.writeU1 (pNumEntryClasses);
    aOut.writeU1 (pLastClass);
    IOUtilities.writePadding (aOut, 2);
  }

  public int getLength()
  {
    return IOUtilities.adjustedSize (16, 2);
  }
}


