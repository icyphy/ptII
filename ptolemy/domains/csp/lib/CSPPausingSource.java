/* CSPPausingSource atomic actor.

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
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.Token;
import ptolemy.data.IntToken;
import java.util.Random;


//////////////////////////////////////////////////////////////////////////
//// CSPSource
/**
    FIXME: add description!!

@author Neil Smyth
@version $Id$

*/

public class CSPPausingSource extends AtomicActor {
    public CSPPausingSource() {
        super();
    }

    public CSPPausingSource(CompositeActor cont, String name)
            throws IllegalActionException, NameDuplicationException {
         super(cont, name);
         output = new IOPort(this, "output", false, true);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    public void fire() {
        try {
            Random rand = new Random();
            int count = 0;
            while (count < 12 ) {
                //Thread.currentThread().sleep((long)(rand.nextDouble()*1000));
                Token t = new IntToken(count);
                output.send(0,t);
                System.out.println(getName() + " sent Token: " + t.toString());
                if (count % 3 == 0) {
                    try {
                        ((CSPDirector)getDirector()).setPauseRequested();
                        Thread.currentThread().sleep((long)(2000));
                        ((CSPDirector)getDirector()).setResumeRequested();
                    } catch (InterruptedException ex) {
                        System.out.println("CSPPausingSource: unable to " +
                                "complete pause or resume.");
                    }
                }
                count++;
            }
            System.out.println("CSP(" + getName() + "):finished normally.");
            _again = false;
            return;
        } catch (IllegalActionException ex) {
            System.out.println("CSPSource: illegalActionException, exiting");
        }
    }

    public boolean prefire() {
        return _again;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public variables                       ////

    public IOPort output;
    private boolean _again = true;
}
