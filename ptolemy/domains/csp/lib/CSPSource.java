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
    
    public CSPSource(CSPCompositeActor cont, String name, CSPReceiver rec) 
        throws IllegalActionException, NameDuplicationException {
        super(cont, name);
	receiver = rec;
        output = new IOPort(this, "output", false, true);
    }


    public void _run() {
        try {
            int count = 0;
            while (count < 10 ) {
                Token t = new IntToken(count);
                //System.out.println(getName() + ": created token about to send it");
		//output.send(0,t);
		receiver.put(t);
                System.out.println("Source sent Token: " +t.toString() + " to " + getName());
                count++;
            }
            // terminate 
            //output.send(0, new NullToken());
	    receiver.put( new NullToken());
        } catch (Exception ex) {
            System.out.println(getName() + ": " + ex.getMessage() + ":" +ex.getClass().getName());
            //throw new IllegalActionException(this, "error in source");
        }
        return;
    }
    public IOPort output;
    public CSPReceiver receiver;
}
