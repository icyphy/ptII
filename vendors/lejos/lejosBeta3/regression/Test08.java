
// Static initializers

import tinyvm.rcx.ROM;

public class Test08
{
  static
  {
    ROM.playTone (200, 50);
  }

  static class Inner
  {
    static
    {
      ROM.playTone (1000, 50);
    }

    void method()
    {
      ROM.playTone (2000, 50);
    }
  }

  public static void main (String[] aArg)
  {
    new Inner().method();
    for (int i = 0; i < 20000; i++) { }
  }
}
