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
import ptolemy.actor.*;
//import ptolemy.kernel.*;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.Token;

//////////////////////////////////////////////////////////////////////////
//// CSPSink
/**
Accepts Tokens from a single channel. 

@author Neil Smyth
@version $Id$

 */
public class CSPSink extends AtomicActor {
    public CSPSink() {
        super();
    }

    public CSPSink(CompositeActor cont, String name)
        throws IllegalActionException, NameDuplicationException {
        super(cont, name);
        input = new IOPort(this, "input", true, false);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    public void fire() {
        int count = 0;
        try {
            while (count < 10 ) {
                Token t = input.get(0);
                System.out.println(getName() + " received Token: " +
                      t.toString());
                count++;
            }
            System.out.println("CSPSink(" + getName() + "):finished normally.");
            return;
        } catch (IllegalActionException ex) {
            System.out.println("CSPSink invalid get, exiting...");
        } catch (NoTokenException ex) {
            System.out.println("CSPSink invalid get, exiting...");
        }
    }

    public boolean postfire() {
        return false;
    }

   public IOPort input;
}
