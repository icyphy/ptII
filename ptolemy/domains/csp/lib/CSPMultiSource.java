package ptolemy.domains.csp.actors;

import ptolemy.domains.csp.kernel.*;
import ptolemy.actor.*;
import ptolemy.data.*;

public class CSPMultiSource extends CSPActor implements Runnable {
  public CSPMultiSource(String name, CSPReceiver[] recs ) {
    super();
    setName(name);
    receivers = recs;
  }

  public void run() {
    try {
      int count = 0;
      int size = receivers.length;
      int i = 0;
      while (count < 10 ) {
	Token t = new IntToken(count);
	ConditionalBranch[] branches = new ConditionalBranch[size];
	for (i=0; i<size; i++) {
	  branches[i] = new ConditionalSend(receivers[i], this, i, t);
	}
	int successfulBranch = chooseBranch(branches);
	boolean flag = false;
	for (i=0; i<size; i++) {
	  if (successfulBranch == i) {
	    System.out.println(getName() + ": sent Token: " +t.toString() + " to " + receivers[i].getName());
	    flag = true;
	  }
	}
	if (!flag) {
	  System.out.println("Error: successful branch id not valid!");
	}
	count++;
      }
      // terminate sinks
      for (i=0; i<size; i++) {
	receivers[i].put(new NullToken());
      }
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    }
    return;
  }
 
  private CSPReceiver[] receivers;  
}
