
// Test for motor control & exception catching

import tinyvm.rcx.*;

public class Test06
{
  public static void myMethod()
  {
    throw new RuntimeException();
  }

  public static void main (String[] aArg)
  {
    Motor.controlMotor ('A', 1, 1);
    Motor.controlMotor ('C', 1, 7);
    for (int i = 0; i < 10000; i++) {}
    Motor.controlMotor ('A', 3, 1);
    Motor.controlMotor ('C', 3, 1);
    try {
      (new int[1])[1] = 0;
    } catch (ArrayIndexOutOfBoundsException e) {
      Sound.playTone (3000, 100);
    }
    try {
      int[] x = null;
      x[0] = 0;
    } catch (Throwable t) {
      Sound.playTone (2000, 100);
    }
    try {
      myMethod();
    } catch (Exception e) {
      Sound.playTone (1000, 100);
    }
    for (int i = 0; i < 100000; i++) {}
    try {
      int[] x = null;
      x[0] = 0;
    } catch (ArrayIndexOutOfBoundsException e) {
      Sound.playTone (3000, 200);
    } finally {
      Motor.controlMotor ('A', 1, 1);
      for (int i = 0; i < 10000; i++) {}
      Motor.controlMotor ('A', 3, 1);
    }
  }
}


