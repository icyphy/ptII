
import tinyvm.rcx.*;

public class Test19
{
  public static void main (String[] arg)
  {
    int pB1;
    int pB2;

    pB1 = Native.readMemoryByte ((short) 0xffbb) & 0xFF;
    pB2 = Native.readMemoryByte ((short) 0xfd85) & 0xFF;

    Native.writeMemoryByte ((short) 0xfd85, (byte) (pB2 | 0x40));
    Native.writeMemoryByte ((short) 0xffb9, (byte) (pB2 | 0x40));
    Native.writeMemoryByte ((short) 0xfd85, (byte) (pB2 | 0x20));
    Native.writeMemoryByte ((short) 0xffb9, (byte) (pB2 | 0x20));

    Native.writeMemoryByte ((short) 0xffbb, (byte) (pB1 | 0x20));
    Native.writeMemoryByte ((short) 0xffbb, (byte) (pB1 | 0x40));
    Native.writeMemoryByte ((short) 0xffbb, (byte) (pB1 & ~0x40));
    for (int i = 0; i < 0x0E; i++)
    {
      Native.writeMemoryByte ((short) 0xffbb, (byte) (pB1 & ~0x20));
      Native.writeMemoryByte ((short) 0xfd85, (byte) (pB2 | 0x40));
      Native.writeMemoryByte ((short) 0xffb9, (byte) (pB2 | 0x40));
      for (int j = 0; j < 8; j++)
      {
        Native.writeMemoryByte ((short) 0xffbb, (byte) (pB1 & ~0x20));
        Native.writeMemoryByte ((short) 0xffbb, (byte) (pB1 & ~0x40));        
        Native.writeMemoryByte ((short) 0xffbb, (byte) (pB1 | 0x20));
      }
      Native.writeMemoryByte ((short) 0xffbb, (byte) (pB1 | 0x20));
      Native.writeMemoryByte ((short) 0xffbb, (byte) (pB1 & ~0x20));
      Native.writeMemoryByte ((short) 0xfd85, (byte) (pB2 & ~0x40));
      Native.writeMemoryByte ((short) 0xffb9, (byte) (pB2 & ~0x40));
      Native.writeMemoryByte ((short) 0xffbb, (byte) (pB1 | 0x20));
    }
    Native.writeMemoryByte ((short) 0xffbb, (byte) (pB1 & ~0x20));
    Native.writeMemoryByte ((short) 0xfd85, (byte) (pB2 | 0x40));
    Native.writeMemoryByte ((short) 0xffb9, (byte) (pB2 | 0x40));
    Native.writeMemoryByte ((short) 0xffbb, (byte) (pB1 & ~0x40));        
    Native.writeMemoryByte ((short) 0xffbb, (byte) (pB1 | 0x20));
    Native.writeMemoryByte ((short) 0xffbb, (byte) (pB1 | 0x40));        
    for (int k = 0; k < 1000000; k++) { }
  }
}
