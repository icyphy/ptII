package ptolemy.domains.csp.actors;

import ptolemy.domains.csp.kernel.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import java.util.Random;

public class CSPSink extends CSPActor implements Runnable {
  public CSPSink(String name, CSPReceiver rec) {
    super();
    setName(name);
    receiver = rec;
  }

  public void run() {
    int times = 0;
    Random rand = new Random();
    try {
      int count = 0;
      while (count < 1000 ) {
	Token t = receiver.get();
	if (t instanceof NullToken ) {
	  System.out.println("\n" + getName() + ": fired " + times + " times.");
	  return;
	}
	System.out.println(getName() + "  received Token: " + t.toString());
	times++;
	count++;
	Thread.currentThread().sleep((long)(rand.nextDouble()*1000));
      }
    } catch (Exception ex) {
      System.out.println("Error in " + getName() + ": " + ex.getMessage());
    }
 
    return;
  }
  
  private CSPReceiver receiver;
}
