
public class Test31 extends Thread
{
  public void run()
  {
    Thread.yield();
  }
  
  public static void main (String[] arg)
  {
    new Test31().start();
  }
}
