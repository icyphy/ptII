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

public class CSPSource extends AtomicActor {
    public CSPSource() {
        super();
    }

    public CSPSource(CompositeActor cont, String name)
            throws IllegalActionException, NameDuplicationException {
         this(cont, name, -1, 0);
    }

    public CSPSource(CompositeActor cont, String name, int tokenCount, int
           initValue) throws IllegalActionException, NameDuplicationException {
         super(cont, name);
         _tokenLimit = tokenCount;
         _value = initValue;
         output = new IOPort(this, "output", false, true);
    }
    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    public void fire() {
        try {
            Random rand = new Random();
            while ( (_value < _tokenLimit) || (_tokenLimit == -1) ) {
                //Thread.currentThread().sleep((long)(rand.nextDouble()*1000));
                Token t = new IntToken(_value);
                //System.out.println(getName() + " sending...");
                output.send(0,t);
                System.out.println(getName() + " sent Token: " + t.toString());
                _value++;
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
    private int _tokenLimit = -1;
    private int _value = 0;
}
