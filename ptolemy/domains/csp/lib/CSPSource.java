package ptolemy.domains.csp.actors;

import ptolemy.domains.csp.kernel.*;
import ptolemy.actor.*;
import ptolemy.data.*;

public class CSPSource extends CSPActor implements Runnable {
  public CSPSource(String name, CSPReceiver rec) {
    super();
    setName(name);
    receiver = rec;
  }

  public void run() {
    try {
      int count = 0;
      while (count < 10 ) {
	Token t = new IntToken(count);
	receiver.put(t);
	System.out.println("Source sent Token: " +t.toString() + " to " + receiver.getName());
	count++;
      }
      // terminate 
      receiver.put(new NullToken());
    } catch (Exception ex) {
      System.out.println(ex.getMessage() + ":" + ex.getClass().getName());
    }
    return;
  }

  private CSPReceiver receiver;
}
