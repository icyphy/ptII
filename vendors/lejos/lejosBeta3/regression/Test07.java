
// Test for virtual methods

import tinyvm.rcx.*;

public class Test07
{
  public Test07()
  {
    Motor.controlMotor ('A', 1, 1);
    for (int i = 0; i < 10000; i++) {}
    Motor.controlMotor ('A', 3, 1);
  }

  public void virtualMethod (int i)
  {
    LCD.showNumber (i);
  }

  public void virtualMethod2()
  {
    LCD.showNumber (4490);
  }

  public static interface TestInterface
  {
    public void callback();
  }

  public static class Inner extends Test07
  implements TestInterface
  {
    public Inner()
    {
      this (30);
    }

    public void callback()
    {
      Motor.controlMotor ('C', 1, 1);
      for (int i = 0; i < 10000; i++) {}
      Motor.controlMotor ('C', 3, 1); 
    }

    public Inner (int k)
    {
      super();
      Sound.playTone ((short) (k * 20), (short) 50);
    }

    public void virtualMethod (int i)
    {
      super.virtualMethod (i + 2);
      Sound.playTone ((short) i, (short) 200);
    }
  }

  public static void main (String[] aArg)
  {
    Test07 p = new Inner();
    Inner q = (Inner) p;
    if (p == null)
      throw new NullPointerException();
    TestInterface t = (TestInterface) p; 
    if (t == null)
      throw new RuntimeException();
    p.virtualMethod (1800);
    for (int i = 0; i < 50000; i++);
    q.virtualMethod2();
    t.callback();
    for (int i = 0; i < 50000; i++);
  }
}


