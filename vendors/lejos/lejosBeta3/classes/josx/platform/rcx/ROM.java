package josx.platform.rcx;

/**
 * Provides access to ROM routines that
 * fall in a "miscellaneous" category.
 */
public class ROM
{
  /**
   * @return Battery power.
   */
  public static int getBatteryPower()
  {
    synchronized (Native.MEMORY_MONITOR)
    {
      int pAddr = Native.iAuxDataAddr;
      Native.callRom2 ((short) 0x29f2, (short) 0x4001, (short) pAddr);
      return Native.readMemoryShort (pAddr);
    }
  }

  /**
   * Resets two-byte timer in the RCX.
   */
  public static void resetMinuteTimer()
  {
    Native.callRom1 ((short) 0x339a, (short) 0);
  }
}



