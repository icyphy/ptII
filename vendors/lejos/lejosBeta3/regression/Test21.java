
// Basic object creation and field access

import tinyvm.rcx.*;

public class Test21
{
  static native int getDataAddress (Object obj);
  static native void writeMemoryByte (int addr, byte b);
  static native byte readMemoryByte (int addr);

  public static void main (String[] aArg)
  {
    byte[] arr = new byte[4];
    int addr = getDataAddress (arr);
    LCD.showNumber (addr / 10);
    LCD.showProgramNumber (1);
    for (int i = 0; i < 10000; i++) { }
    writeMemoryByte (addr + 3, (byte) 25);
    LCD.showNumber (arr[3]);
    LCD.showProgramNumber (2);
    for (int i = 0; i < 100000; i++) { }
  }
}


