package tinyvm.rcx;

/**
 * Abstraction for a sensor. Example:<p>
 * <code><pre>
 *   Sensor.S1.activate();
 *   Sensor.S1.addSensorListener (new SensorListener() {
 *     public void stateChanged (Sensor src, boolean value) {
 *       if (value)
 *         Sound.beep();
 *     }
 *
 *     public void stateChanged (Sensor src, int value) {
 *       LCD.showNumber (value);
 *       for (int k = 0; k < 10; k++) { }
 *     }
 *   });
 *     
 * </pre></code>
 */
public class Sensor
{
  private short iRomId;
  private short iNumListeners = 0;
  private short iPrevRaw;
  private boolean iPrevBoolean;
//   private boolean iCheckRaw = false;
//   private boolean iCheckBoolean = true;
  private Thread iThread;
  private final SensorListener[] iListeners = new SensorListener[4];

  /**
   * Sensor 1.
   */
  public static final Sensor S1 = new Sensor (1);
  /**
   * Sensor 2.
   */
  public static final Sensor S2 = new Sensor (2);
  /**
   * Sensor 3.
   */
  public static final Sensor S3 = new Sensor (3);

  private Sensor (int aId)
  {
    iRomId = (short) (0x1000 + aId - 1);
  }

//   /**
//    * Sets a flag indicating whether listeners
//    * should be informed when the raw valud of
//    * the sensor changes. This is <code>false</code>
//    * by default.
//    */
//   public final void setCheckRaw (boolean aValue)
//   {
//     iCheckRaw = aValue;
//   }

//   /**
//    * Sets a flag indicating whether listeners
//    * should be informed when the boolean value of
//    * the sensor changes. This is <code>true</code>
//    * by default.
//    */
//   public final void setCheckBoolean (boolean aValue)
//   {
//     iCheckBoolean = aValue;
//   }
  
  /**
   * Adds a sensor listener.
   * <p>
   * <b>
   * NOTE 1: You can add at most 4 listeners.<br>
   * NOTE 2: Calling this method will result in the creation of
   * a non-daemon thread (one per sensor at most), i.e. your 
   * program will not terminate on its own.<br>
   * NOTE 3: Synchronizing inside listener methods could result
   * in a deadlock.
   * </b>
   */
  public synchronized void addSensorListener (SensorListener aListener)
  {
    // Hack: Make sure Native is initialized before thread is created.
    Native.getDataAddress (null);
    if (iThread == null)
    {
      iThread = new Thread()
      {
        public void run()
	{
          for (;;)
	  {
              int pRaw = readRawValue();
              if (iPrevRaw != pRaw)
	      {
                // This kind of synchronization could
                // interfere with user's monitors.
                synchronized (Sensor.this)
		{
                  for (int i = 0; i < iNumListeners; i++)
                    iListeners[i].stateChanged (Sensor.this, pRaw);
		}
	      }
              iPrevRaw = (short) pRaw;
              boolean pBoolean = readBooleanValue();
              if (iPrevBoolean != pBoolean)
	      {
                synchronized (Sensor.this)
		{
	          Thread.yield();			
                  for (int i = 0; i < iNumListeners; i++)
		  {
                    iListeners[i].stateChanged (Sensor.this, pBoolean);
		  }
		}
	      }
              iPrevBoolean = pBoolean;            
	      for (short i = 10; i-- > 0; )
		Thread.yield();
	  }     
	}
      };
    }
    if (!iThread.isAlive())
    {
      iThread.start();
      iNumListeners = 0;
    }
    iListeners[iNumListeners++] = aListener;
  }

  /**
   * Activates the sensor. This method should be called
   * if you want to get accurate values from the
   * sensor. In the case of light sensors, you should see
   * the led go on when you call this method.
   */
  public final void activate()
  {
    Native.callRom1 ((short) 0x1946, (short) iRomId);
  }

  /**
   * Passivates the sensor. 
   */
  public final void passivate()
  {
    Native.callRom1 ((short) 0x19C4, (short) iRomId);
  }

  public final int readRawValue()
  {
    return (int) readSensorValue (iRomId, (byte) 0, (byte) 0x00);
  }

  public final boolean readBooleanValue()
  {
    return readSensorValue (iRomId, (byte) 1, (byte) 0x20) != 0;
  }

  public final int readPercentage()
  {
    return (int) readSensorValue (iRomId, (byte) 3, (byte) 0x80);
  }

  /**
   * <i>Low-level API</i> for reading sensor values.
   * @param aCode Sensor ID (0x1000 + num_in_rcx - 1).
   * @param aType 0 = RAW, 1 = TOUCH, 3 = LIGHT.
   * @param aMode 0x00 = RAW, 0x20 = BOOL, 0x80 = PERCENT.
   */
  public static int readSensorValue (short aCode, byte aType, byte aMode)
  {
    // In C: return __rcall2 (0x14c0, code, (short) sensor);
    synchronized (Native.MEMORY_MONITOR)
    {
      byte[] pData = Native.iAuxData;
      // Set type (1 byte)
      pData[0] = aType;
      // Set mode (1 byte)
      pData[1] = aMode;
      // Call read_sensor_value...
      Native.callRom2 ((short) 0x14C0, aCode, (short) Native.iAuxDataAddr);
      int pIntMode = aMode & 0xFF;
      if (pIntMode == 0x00)
        return ((pData[2] & 0xFF) << 8) | (pData[3] & 0xFF);
      if (pIntMode == 0x20)
        return (pData[6] & 0xFF);
      if (pIntMode == 0x80)
        return ((pData[4] & 0xFF) << 8) | (pData[5] & 0xFF);
      return 0;
    }
  }

//   public static int readSensorValue (short aCode, byte aType, byte aMode)
//   {
//     // In C: return __rcall2 (0x14c0, code, (short) sensor);
//     // Address 0xEEF8 is used to store sensor data.
//     synchronized (Native.MEMORY_MONITOR)
//     {
//       // Set type (1 byte)
//       Native.writeMemoryByte (0xEEF8 + 0, aType);
//       // Set mode (1 byte)
//       Native.writeMemoryByte (0xEEF8 + 1, aMode);
//       // Call read_sensor_value...
//       Native.callRom2 ((short) 0x14C0, aCode, (short) 0xEEF8);
//       int pIntMode = aMode & 0xFF;
//       if (pIntMode == 0x00)
//         return Native.readMemoryShort (0xEEF8 + 2);
//       if (pIntMode == 0x20)
//         return Native.readMemoryByte (0xEEF8 + 6) & 0xFF;
//       if (pIntMode == 0x80)
//         return Native.readMemoryShort (0xEEF8 + 4);
//       return 0;
//     }
//   }
}







