
// Simple anonymous threads

import tinyvm.rcx.*;

public class Test11
{ 
  public static void main (String[] argv)
  {
    new Thread() {
      public void run() {
        for (int i = 0; i < 5; i++)
	{
          Sound.playTone (2000, 20);
          for (int k = 0; k < 100; k++) {}
	}
      }
    }.start();

    new Thread() {
      public void run() {
        for (int i = 0; i < 5; i++)
	{
          Sound.playTone (200, 20);
          for (int k = 0; k < 100; k++) {}
	}
      }
    }.start();

    for (int i = 0; i < 100000; i++) {}
  }
}
