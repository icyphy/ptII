
import josx.platform.rcx.*;

/**
 * DESCRIPTION
 *   Program that makes PATHFINDER learn to rotate
 *   by simply using a light sensor to scan normally
 *   diverse surroundings. 
 *
 * CAUTION
 *   The wheels of your robot may fall off after
 *   several rotations.
 *
 * REQUIREMENS
 *   - A simple robot that can rotate, with a 
 *     configuration equivalent to the "PATHFINDER"
 *     (RIS Constructopedia, page 6). You may want
 *     to try different wheel sizes and surfaces 
 *     to check that the program really works.
 *   - A light sensor connected to sensor input 2.
 *     The light sensor should be pointing in a direction
 *     parallel to the floor.
 *   - You may need to change the SAMPLINGS and MOTOR_POWER
 *     constants below. I tried my experiments on
 *     carpet. For smooth surfaces, I suspect you'd
 *     have to set SAMPLINGS to 1, and MOTOR_POWER to 1. 
 *     The idea is that the robot should perform test 
 *     rotations between 2 * 360 and 4 * 360 degrees. Anything 
 *     outside of that range could produce erroneous results.
 *     In other words, the second and fourth
 *     flashing numbers you see should be between
 *     2000 and 4000.
 *
 * HOW TO RUN
 *   - You should read $LEJOS_HOME/README if you
 *     still haven't.
 *   - Set CLASSPATH to . (dot).
 *   - Compile the program: lejosc Rotator.java
 *   - Link the class     : lejos Rotator -o Rotator.tvm
 *   - Download leJOS firmware if necessary.
 *   - Make sure the robot is about 3 feet away from anything
 *     else. Make the sorroundings somewhat varied. I use
 *     a flashlight, but it looks like this isn't really
 *     necessary. Don't move too much.
 *   - Run linked binary  : lejosrun Rotator.tvm.
 *     The download time is about 30 seconds.
 * 
 * WHAT IT DOES
 *   - Performs one rotation of more than 720 degrees.
 *     The robot starts to scan its sorroundings with
 *     the light sensor (hopefully) only after the
 *     motors have achieved constant speed. Does
 *     some analysis for a few seconds.
 *   - Performs another rotation, but in 5 steps.
 *     Scans basically all the way through.
 *   - Analyzes the data obtained in the two trial runs,
 *     and determines the constant motor velocity,
 *     and the startup delay factor.
 *   - Makes the robot move in a straight line,
 *     turn 180 degrees, and move in the opposite
 *     direction. It repeats this 4 times.
 *     The error for a 180 degrees rotation is
 *     probably about 5% to 10%, on carpet.
 */
public class Rotator
implements SensorConstants
{
  // You'll probably need to change these
  // constants. Note that LENGTH is
  // the length of an array, and TinyVM does
  // not accept arrays longer than 255 elements.

  private static final int SAMPLINGS = 10;
  private static final int LENGTH = 240;
  private static final int MATCHLEN = 10;
  private static final int MOTOR_POWER = 3;
  static int[] iSamples = new int[LENGTH];

  public static void scan (int aIndex, int aLength)
  {
    // Start motors
    Motor.A.setPower (MOTOR_POWER);
    Motor.C.setPower (MOTOR_POWER);
    Motor.A.forward();
    Motor.C.backward();
    // Start sampling
    int pTop = aIndex + aLength;
    for (int i = aIndex; i < pTop; i++)
    {
      iSamples[i] = 0;
      for (int j = 0; j < SAMPLINGS; j++)
        iSamples[i] += Sensor.S2.readValue();
    }
    Motor.A.stop();
    Motor.C.stop();
  }

  public static void delayedScan (int aIndex, int aLength)
  {
    // Start motors
    Motor.A.setPower (MOTOR_POWER);
    Motor.C.setPower (MOTOR_POWER);
    Motor.A.forward();
    Motor.C.backward();
    // Delay enough so that motors are rotating at constant speed
    for (int k = 0; k < 20000; k++) {}
    // Start sampling
    int pTop = aIndex + aLength;
    for (int i = aIndex; i < pTop; i++)
    {
      iSamples[i] = 0;
      for (int j = 0; j < SAMPLINGS; j++)
        iSamples[i] += Sensor.S2.readValue();
    }
    Motor.A.stop();
    Motor.C.stop();
  }

  /**
   * Returns average difference of sample comparison * 100.
   */
  public static int scoreMatch (int start1, int start2, int aLength)
  {
    int pCumulDiff = 0;
    int i = start1;
    int j = start2;
    while (i < start2 && j < aLength)
    {
      int pI = iSamples[i];
      int pJ = iSamples[j];
      pCumulDiff += pI > pJ ? (pI - pJ) : (pJ - pI);
      i++;
      j++;
    }
    return (pCumulDiff * 100) / (i - start1);
  }

  public static int analyze (int aLength)
  {
    int pMinDiff = 1000000;
    int pMinIndex = 0;
    for (int j = 10; j < aLength / 2; j++)
    {
      int pDiff = scoreMatch (0, j, aLength);
      if (pDiff < pMinDiff)
      {
        pMinDiff = pDiff;
        pMinIndex = j;
      }
    }
    return pMinIndex;
  }

  static void beepAndDelay()
  {
    Sound.playTone (1000, 10);
    for (int k = 0; k < 10000; k++) { }
    LCD.showNumber (8888);
    LCD.showProgramNumber (8);
  }

  static void flashNumbers (int aNum, int aPrg)
  throws Exception
  {
    for (int i = 0; i < 4; i++)
    {
      LCD.showNumber (aNum);
      LCD.showProgramNumber (aPrg);
      Thread.sleep (400);
      LCD.clear();
      LCD.refresh();
      Thread.sleep (100);
    }
  }

  public static void main (String[] arg)
  throws Exception
  {
    // [1] FIRST SAMPLING

    beepAndDelay();

    Sensor.S2.setTypeAndMode (SENSOR_TYPE_LIGHT, SENSOR_MODE_PCT);
    Sensor.S2.activate();
    delayedScan (0, LENGTH);
    Sensor.S2.passivate();
   
    beepAndDelay();

    int pCirc = analyze (LENGTH);
    int pD = (LENGTH * 10000 + (pCirc / 2)) / pCirc;
    int pVel = (pD + (LENGTH / 2)) / LENGTH;

    flashNumbers (pCirc, 1);
    flashNumbers (pD / 10, 2);

    // [2] SECOND SAMPLING

    Sensor.S2.activate();
    for (int i = 0; i < 5; i++)
    {
      scan (i * 40, 40);
      // Delay here a bit
      for (int k = 0; k < 10000; k++) {}
    }
    Sensor.S2.passivate();

    beepAndDelay();

    pCirc = analyze (LENGTH);
    int pD2 = (LENGTH * 10000 + (pCirc / 2)) / pCirc;

    flashNumbers (pCirc, 3);
    flashNumbers (pD2 / 10, 4);
    
    // pD2 is pretty accurately the actual rotational distance
    // of the second sampling. We divide it by 5, and we get
    // the distance for each step.

    pD2 = (pD2 + 2) / 5;
    int pDelaySpace = pVel * 40 - pD2;
    beepAndDelay();
    flashNumbers (pDelaySpace / 10, 5);

    beepAndDelay();

    int pT = (5000 + pDelaySpace + (pVel / 2)) / pVel;
    for (int rot = 0; rot < 4; rot++) 
    {
      Motor.A.forward();
      Motor.C.forward();
      for (int k = 0; k < 7000; k++) {}
      Motor.A.stop();
      Motor.C.stop();
      beepAndDelay();
      scan (0, pT);
      beepAndDelay();
    }
  }
}





