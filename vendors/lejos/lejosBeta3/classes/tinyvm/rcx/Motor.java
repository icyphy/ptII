package tinyvm.rcx;

/**
 * Abstraction for a motor. Example:<p>
 * <code><pre>
 *   Motor.A.setPower(1);
 *   Motor.C.setPower(7);
 *   Motor.A.forward();
 *   Motor.C.forward();
 *   for (int i = 0; i < 1000; i++) { }
 *   Motor.A.stop();
 *   Motor.C.stop();
 * </pre></code>
 */
public class Motor
{
  private char  iId;
  private short iMode = 3;
  private short iPower = 3;

  /**
   * Motor A.
   */
  public static final Motor A = new Motor ('A');
  /**
   * Motor B.
   */
  public static final Motor B = new Motor ('B');
  /**
   * Motor C.
   */
  public static final Motor C = new Motor ('C');

  private Motor (char aId)
  {
    iId = aId;
  }

  /**
   * Sets motor power to a <i>value between 0 and 7</i>.
   * @param A value in the range [0-7].
   */
  public final void setPower (int aPower)
  {
    iPower = (short) aPower;
    controlMotor (iId, iMode, aPower);
  }

  /**
   * Causes motor to rotate forward.
   */
  public final void forward()
  {
    iMode = 1;
    controlMotor (iId, 1, iPower);
  }

  /**
   * Causes motor to rotate backwards.
   */
  public final void backward()
  {
    iMode = 2;
    controlMotor (iId, 2, iPower);
  }

  /**
   * Causes motor to stop.
   */
  public final void stop()
  {
    iMode = 3;
    controlMotor (iId, 3, 7);
  }

  /**
   * Causes motor to float.
   */
  public final void flt()
  {
    iMode = 4;
    controlMotor (iId, 4, iPower);
  }

  /**
   * <i>Low-level API</i> for controlling a motor.
   * @param aMotor The motor id: 'A', 'B' or 'C'.
   * @param aMode 1=forward, 2=backward, 3=stop, 4=float
   * @param aPower A value in the range [0-7].
   */
  public static void controlMotor (char aMotor, int aMode, int aPower)
  {
    Native.callRom3 ((short) 0x1a4e, (short) (0x2000 + aMotor - 'A'), 
                    (short) aMode, (short) aPower);
  }
}







