/* CSPMultiSink atomic actor.

 Copyright (c) 1998 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (nsmyth@eecs.berkeley.edu)

*/

package ptolemy.domains.csp.lib;

import ptolemy.domains.csp.kernel.*;
import ptolemy.data.*;
import java.util.Random;

//////////////////////////////////////////////////////////////////////////
//// CSPMultiSink
/** 
    FIXME: add description!!

@author Neil Smyth
@version $Id$

*/
public class CSPMultiSink extends CSPActor {
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
