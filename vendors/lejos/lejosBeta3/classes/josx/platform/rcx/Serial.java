package josx.platform.rcx;

/**
 * Low-level API for infra-red (IR) communication between
 * an RCX and the IR tower or between two RCXs.
 * For protocol details, Kekoa Proudfoot's Opcode Reference
 * is highly recommended. See Kekoa's 
 * <a href="http://graphics.stanford.edu/~kekoa/rcx/index.html">RCX Internals</a> page.
 * Kekoa Proudfoot has also written a C based tool
 * (<a href="http://graphics.stanford.edu/~kekoa/rcx/tools.html">Send</a>)
 * which you can use to send packets to the RCX. If you prefer
 * to write everything in Java, you should become familiar with the
 * <a href="http://java.sun.com/products/javacomm/">Java Communications API</a>.
 * Frameworks based on this API have already been developed
 * by Dario Laverde 
 * (see <a href="http://www.escape.com/~dario/java/rcx/">RCXLoader</a>)
 * and Scott Lewis (see <a href="http://www.slewis.com/rcxport/">RCXPort</a>).
 * The Java Communications API is officially supported on Windows and Solaris.
 * <p>
 * Examples that use the leJOS Serial class can be found in:
 * <ul>
 * <li><code>examples/serial</code> --- Receiver for certain opcodes, such as MotorOn.
 * <li><code>examples/serial2rcx</code> --- Communication between two RCXs.  
 * <li><code>examples/remotectl</code> --- Receiver for Lego remote control.
 * </ul>
 * <p>
 * The basic pattern for a Receiver is:
 * <p>
 * <code>
 * <pre>
 *    byte[] packet = new byte[8];
 *    for (;;)
 *    {
 *      if (Serial.isPacketAvailable())
 *      {
 *        Serial.readPacket (packet);
 *        byte opcode = packet[0];
 *        if (opcode == AN_OPCODE)
 *          ...
 *          ...
 *        // Possibly send a response here
 *        packet = ~packet[0];
 *        Serial.sendPacket (packet, 0, PACKET_LENGTH);
 *      }
 *    }
 * </pre>
 * </code>
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
   * 0x45 (Transfer Data) is received in a special way: If you 
   * had previously called setDataBuffer(), packet data will
   * be copied into the buffer provided. Note the caveats regarding
   * setDataBuffer() use.
   *
   * @return The number of bytes received.
   * @see josx.platform.rcx.Serial#isPacketAvailable
   * @see josx.platform.rcx.Serial#setDataBuffer
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
   * Sends a packet to the IR tower or another RCX.
   * In general, the IR tower will only receive <i>responses</i>
   * to messages it has sent.
   */
  public static void sendPacket (byte[] aBuffer, int aOffset, int aLen)
  {
    Native.callRom4 ((short) 0x343e, (short) 0x1775, (short) 0, 
                     (short) (Native.getDataAddress (aBuffer) + aOffset),
                     (short) aLen);
  }

  /**
   * Sets long range transmision.
   */
  public static void setRangeLong()
  {
    Native.callRom1 ((short) 0x3250, (short) 0x1770);	  
  }

  /**
   * Sets short range transmision.
   */
  public static void setRangeShort()
  {
    Native.callRom1 ((short) 0x3266, (short) 0x1770);	  
  }
  
  /**
   * Resets serial communications. It can be used
   * to disable buffers set with <code>setDataBuffer</code>.
   */
  public native static void resetSerial();

}


