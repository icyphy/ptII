import tinyvm.rcx.*;

// Test for anewarray

public class Test27
{
  int i;

  public Test27 (int a)
  {
    i = a;
  }

  public void method()
  {
    LCD.showNumber (i);
  }

  public static void main (String[] arg) 
  {
    Test27[] arr = new Test27[2];
    arr[0] = new Test27 (44);
    arr[1] = new Test27 (90);
    arr[0].method();
    arr[1].method();
  }
}
