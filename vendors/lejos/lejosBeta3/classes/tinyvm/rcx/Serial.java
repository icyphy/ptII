package tinyvm.rcx;

/**
 * Low-level APIs for serial communications.
 * For details, Kekoa Proudfoot's
 * RCX opcode reference is highly recommended.
 */
public class Serial
{
  private static final byte[] iAuxBuffer = new byte[4];
  private static final int iAuxBufferAddr = Native.getDataAddress (iAuxBuffer);

  private Serial()
  {
  }

  /**
   * Reads a packet received by the RCX, if one is available.
   * The first
   * byte in the buffer is the opcode. Opcode
   * 0x45 (Transfer Data) is received in a special way: Use 
   * setDataBuffer().
   *
   * @return The number of bytes received.
   * @see tinyvm.rcx.Serial#isPacketAvailable
   * @see tinyvm.rcx.Serial#setDataBuffer
   */
  public static int readPacket (byte[] aBuffer)
  {
    synchronized (Native.MEMORY_MONITOR)
    {
      // Receive packet data
      iAuxBuffer[2] = (byte) 0;
      Native.callRom3 ((short) 0x33b0, (short) Native.getDataAddress (aBuffer),
                       (short) aBuffer.length, (short) (iAuxBufferAddr + 2));
      return (int) iAuxBuffer[2];
    }
  }

  /**
   * Sets the buffer that will be used to save data
   * transferred with opcode 0x45.<p>
   * <b>Note:</b> This method must be used with caution.
   * A pointer to the data buffer is passed to the ROM
   * for asynchronous use.
   * If more data is received than can be stored in the
   * buffer, the VM's memory will be corrupted and
   * it will crash or at least misbehave.
   */
  public static void setDataBuffer (byte[] aData)
  {
    // Set data pointer
    Native.callRom3 ((short) 0x327c, (short) 0x1771, 
                     (short) Native.getDataAddress (aData), (short) 0);
  }

  /**
   * Checks to see if a packet is available.
   * Call this method before calling receivePacket.
   */
  public static boolean isPacketAvailable()
  {
    synchronized (Native.MEMORY_MONITOR)
    {
      // Check for data
      Native.callRom2 ((short) 0x3426, (short) (iAuxBufferAddr + 3), (short) 0);
      return (iAuxBuffer[3] != 0);
    }
  }

  /**
   * Sends a packet.
   */
  public static void sendPacket (byte[] aBuffer, int aOffset, int aLen)
  {
    Native.callRom4 ((short) 0x343e, (short) 0x1775, (short) 0, 
                     (short) (Native.getDataAddress (aBuffer) + aOffset),
                     (short) aLen);
  }

  /**
   * Resets serial communications.
   */
  public static void resetRcx()
  {
    resetSerial();
  }

  
  private native static void resetSerial();
}


