/* CSPSink atomic actor.

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
//// CSPSink
/** 
    FIXME: add description!!

@author Neil Smyth
@version $Id$

 */
public class CSPSink extends CSPActor {
    public CSPSink() {
        super();
    }
    
    public CSPSink(CSPCompositeActor cont, String name, CSPReceiver rec) 
        throws IllegalActionException, NameDuplicationException {
        super(cont, name);
	receiver = rec;
        input = new IOPort(this, "input", true, false);
    }

    public void _run() {
        int times = 0;
        Random rand = new Random();
        try {
            int count = 0;
            while (count < 1000 ) {
	         Token t = input.get(0);
                 //Token t = receiver.get();
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
    
    public IOPort input;
    public CSPReceiver receiver;
}
