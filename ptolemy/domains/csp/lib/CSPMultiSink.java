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
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
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
    public CSPMultiSink() {
        super();
    }
    
    public CSPMultiSink  (CSPCompositeActor cont, String name)
       throws IllegalActionException, NameDuplicationException {
	 super(cont, name);
	 input = new IOPort(this, "input", true, false);
	 input.makeMultiport(true);
    }

  public void _run() {
    try {
      int count = 0;
      int size = input.getWidth();
      int i = 0;
      boolean[] bools = new boolean[size];
      for (i=0; i<size; i++) {
	bools[i] = true;
      }
      while (count < 25 ) {
	ConditionalBranch[] branches = new ConditionalBranch[size];
	for (i=0; i<size; i++) {
	  if (bools[i]) {
	    branches[i] = new ConditionalReceive(input, i, i);
	  }
	}
	int successfulBranch = chooseBranch(branches);
	boolean flag = false;
	for (i=0; i<size; i++) {
	  if (successfulBranch == i) {
	    System.out.println(getName() + ": received Token: " + getToken().toString() + " from receiver " + i);
	    flag = true;
	    }
	}
	if (!flag) {
	  System.out.println("Error: successful branch id not valid!");
	}
	count++;
      }
    
    } catch (IllegalActionException ex) {
      System.out.println("Error: could not create ConditionalReceive branch");
    }
    return;
  }
 
  public IOPort input;
}
