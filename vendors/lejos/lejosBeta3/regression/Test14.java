
// Stack overflow test

import tinyvm.rcx.*;

public class Test14
{ 
  static int i = 0;
  
  public static void recurse()
  {
    i++;
    recurse();
  }

  public static void main (String[] argv)
  {
    try {
      recurse();
    } finally {
      LCD.showNumber (i);
      for (int k = 0; k < 50000; k++) {}
    }    
  }
}

