/* CSPSource atomic actor.

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
import ptolemy.actor.IOPort;
import ptolemy.data.*;
import java.util.Random;

//////////////////////////////////////////////////////////////////////////
//// CSPSource
/** 
    FIXME: add description!!

@author Neil Smyth
@version $Id$

*/

public class CSPSource extends CSPActor {
  public CSPSource() {
        super();
    }
    
    public CSPSource(CSPCompositeActor cont, String name) 
        throws IllegalActionException, NameDuplicationException {
        super(cont, name);
        output = new IOPort(this, "output", false, true);
    }

  ////////////////////////////////////////////////////////////////////////
  ////                         protected methods                      ////
 
    protected void _run() {
    try {
	Random rand = new Random();
	int count = 0;
	while (count < 15 ) {
            //Thread.currentThread().sleep((long)(rand.nextDouble()*1000));
	  Token t = new IntToken(count);
	  output.send(0,t);
	  System.out.println(getName() + " sent Token: " + t.toString());
	  count++;
	}
	return;
      } catch (IllegalActionException ex) {
	System.out.println("CSPSource: illegalActionException, exiting");
      }  catch (CloneNotSupportedException ex) {
	System.out.println(getName() + ": cannot clone  token, bug in DATA");
        /*} catch (InterruptedException ex) {
	System.out.println(getName() + ": interupted while sleeping");
        */}
    }
    
    public IOPort output;
}
