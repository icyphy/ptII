package js.classfile;

public class JUtf8
{
  public static int getMaxLength (String aString)
  {
    // This can be improved considerably
    return (aString.length() * 3);
  }

  public static int stringToUtf8 (String aString, byte aBytes[], int aOff)
  {
    int pTop = aString.length();
    for (int pCount = 0; pCount < pTop; pCount++)
    {
      aOff = charToUtf8 (aString.charAt(pCount), aBytes, aOff);
    }
    return aOff;
  }

  private static int charToUtf8 (char aChar, byte aBytes[], int aOff)
  {
    if (aChar >= '\u0001' && aChar <= '\u007F')
      aBytes[aOff++] = (byte) aChar;
    else if (aChar == '\u0000' || (aChar >= '\u0080' && aChar <= '\u07FF'))
    {
      aBytes[aOff++] = (byte) (0xC0 | ((aChar & 0x07C0) >> 6));
      aBytes[aOff++] = (byte) (0x80 | (aChar & 0x003F));
    }
    else
    {
      aBytes[aOff++] = (byte) (0xE0 | ((aChar & 0xF000) >> 12));
      aBytes[aOff++] = (byte) (0x80 | ((aChar & 0x0FC0) >> 6));
      aBytes[aOff++] = (byte) (0x80 | (aChar & 0x003F)); 
    }
    return aOff;
  }

  private static int getUtf8CharLen (byte aBytes[], int aOff)
  {
    if ((aBytes[aOff] & 0xF0) == 0xE0)
      return 3;
    if ((aBytes[aOff] & 0xE0) == 0xC0)
      return 2;
    return 1;
  }

  private static char Utf8ToChar (byte aBytes[], int aOff)
  {
    if ((aBytes[aOff] & 0xF0) == 0xE0)
      return (char) (((aBytes[aOff] & 0x0F) << 12) +
             ((aBytes[aOff+1] & 0x3F) << 6) +
             (aBytes[aOff+2] & 0x3F));
    
    if ((aBytes[aOff] & 0xE0) == 0xC0)
      return (char) (((aBytes[aOff] & 0x1F) << 6) +
             (aBytes[aOff+1] & 0x3F));
    return (char) aBytes[aOff];
  }

  public static String Utf8ToString (byte aBytes[], int aOff, int aLength)
  {
    char pChars[] = new char[aLength];
    int pIndex = 0;
    int pTop = aOff + aLength;
    while (aOff < pTop)
    {
      int pLen = getUtf8CharLen (aBytes, aOff);
      pChars[pIndex] = Utf8ToChar (aBytes, aOff);
      aOff += pLen;
      pIndex++;
    }
    return new String (pChars, 0, pIndex);
  }
}




