package ptolemy.domains.csp.actors;

import ptolemy.domains.csp.kernel.*;
import ptolemy.actor.*;
import ptolemy.data.*;

public class CSPMultiSink extends CSPActor implements Runnable {
  public CSPMultiSink(String name, CSPReceiver[] recs ) {
    super();
    setName(name);
    receivers = recs;
  }

  public void run() {
    try {
      int count = 0;
      int size = receivers.length;
      int i = 0;
      boolean[] bools = new boolean[size];
      for (i=0; i<size; i++) {
	bools[i] = true;
      }
      int recAlive = size;
      while (count < 1000 ) {
	ConditionalBranch[] branches = new ConditionalBranch[size];
	for (i=0; i<size; i++) {
	  if (bools[i]) {
	    branches[i] = new ConditionalReceive(receivers[i], this, i);
	  }
	}
	int successfulBranch = chooseBranch(branches);
	boolean flag = false;
	for (i=0; i<size; i++) {
	  if (successfulBranch == i) {
	    System.out.println(getName() + ": received Token: " +getToken().toString() + " from " + receivers[i].getName());
	    flag = true;
	    if (getToken() instanceof NullToken) {
	      System.out.println(getName() + ": finished on branch " + i);
	      bools[i] = false;
	      recAlive--;
	    }
	  }
	}
	if (!flag) {
	  System.out.println("Error: successful branch id not valid!");
	}
	if (recAlive ==0 ) {
	  System.out.println(getName() + ": finished");
	  return;
	}
	count++;
      }
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    }
    return;
  }
 
  private CSPReceiver[] receivers;  
}
