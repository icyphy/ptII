import tinyvm.rcx.*;

// Multi-dimensional arrays

public class Test26
{
  static byte[][] mda1;
  static int[][][] mda2;

  public static void main (String[] arg) 
  {
    mda1 = new byte[5][6];
    for (int i = 0; i < 5; i++)
      for (int j = 0; j < 6; j++)
         mda1[i][j] = (byte) (i * 5 + j);
    mda2 = new int[10][][];
    for (int i = 0; i < 10; i++)
    {
      mda2[i] = new int[5][5];
      for (int j = 0; j < 5; j++)
        for (int k = 0; k < 5; k++)  
          mda2[i][j][k] = mda1[j][k] * i;
    }
    for (int i = 0; i < 10; i++)
      for (int j = 0; j < 5; j++)
        for (int k = 0; k < 5; k++)  
	{
          LCD.showNumber (mda2[i][j][k]);
          for (int l = 0; l < 100; l++) {}
	}
  }
}




