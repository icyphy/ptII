package js.classfile;

import java.util.*;
import java.lang.reflect.*;

public class JMisc
{
   public static long power (long aA, int aB)
   {
       long pTotal = 1;

       for (int pCtr = 0; pCtr < aB; pCtr++)
          pTotal = pTotal * aA;
       return pTotal;
   }

  public static int pow2 (int aN)
  {
      int pRet = 1;
      for (int pCount = 0; pCount < aN; pCount++)
         pRet = pRet << 1;
      return pRet;
  }

  public static int log2 (int aN)
  {
    int pCount = -1;
    while (aN != 0)
      {
        aN = aN >> 1;
        pCount++;
      }
    return pCount;
  }

  public static int getNumBitsFor (int aNumKeys)
  {
      // This should be the ceiling of log2(aNumKeys).
      if (aNumKeys <= 1)
         return 0;
      return log2 (aNumKeys - 1) + 1;
  }

  public static boolean equalChars (char aChar1, char aChar2, boolean aCase)
  {
    return aCase ? (aChar1 == aChar2) :
           (Character.toUpperCase(aChar1) == Character.toUpperCase(aChar2)) ;
  }

  public static int findChar (String aText, int aStart, char aChar)
  {
     int pTLen = aText.length();
     for (int pT = aStart; pT < pTLen; pT++)
        if (aText.charAt(pT) == aChar)
           return pT;
     return -1;
  }

  public static int findCharNoCase (String aText, int aStart, char aChar)
  {
    int pTLen = aText.length();
    for (int pT = aStart; pT < pTLen; pT++)
      if (Character.toUpperCase (aText.charAt(pT)) == Character.toUpperCase(aChar))
        return pT;
    return -1;
  }

    public static int findSubStr (String aText, int aStart, String aSubStr, boolean aCase)
    {
        // Returns index of substring or -1 if not found.
        int pT = aStart;
        int pTLen = aText.length();
        int pSLen = aSubStr.length();
        if (pSLen == 0)
           return -1;
        char pFirst = aSubStr.charAt(0);  
        while (pT < pTLen)
        {
           pT = aCase ? findChar (aText, pT, pFirst) : findCharNoCase (aText, pT, pFirst);
           if (pT == -1)
              return -1;
           int pIndex = pT + 1;
           int pS = 1;
           while (pIndex < pTLen && pS < pSLen)
           {
              if (!equalChars (aText.charAt(pIndex), aSubStr.charAt(pS), aCase))
                 break;
              pIndex++;
              pS++;
           }
           if (pS == pSLen)
              return pT;
           pT++;
           pS = 0;
        }
        return -1;
    }

    public static boolean equalArrays (byte aArray1[], int aOff1, int aLen1,
                                                        byte aArray2[], int aOff2, int aLen2)
    {
        if (aLen1 != aLen2)
          return false;
        for (int pIndex = 0; pIndex < aLen1; pIndex++)
           if (aArray1[aOff1+pIndex] != aArray2[aOff2+pIndex])
              return false;
        return true;
     }

    public static int hashSignature (byte aArray[], int aOff, int aLen)
    {
        int    pActLen = aOff + aLen;
        byte pByte0 = 0;
        byte pByte1 = 0;
        byte pByte2 = 0;
        byte pByte3 = 0;

        for (int pIndex = aOff; pIndex < pActLen; pIndex++)
        {
            pByte0 = (byte) ((pByte0 + aArray[pIndex]) & 0xFF);
            pByte1 ^= aArray[pIndex];
            if ((pIndex % 2) == 0)
            {
                pByte2 &= aArray[pIndex];
                pByte3 |= aArray[pIndex];
            }
            else
            {
                pByte2 |= aArray[pIndex];
                pByte3 &= aArray[pIndex];
            }
        }
        return (pByte0 << 24) | (pByte1 << 16) | (pByte2 << 8) | pByte3;
    }
}
