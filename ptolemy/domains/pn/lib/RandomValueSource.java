/* Generates random valued doubles between 0 and 1 at every unit time.

 Copyright (c) 1997-1999 The Regents of the University of California.
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
*/

package ptolemy.domains.pn.lib;
import ptolemy.domains.pn.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.media.*;
import java.io.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// RandomSource
/** 
Generates randomly generated doubles every time unit.

@author Mudit Goel
@version $Id$
*/

public class RandomValueSource extends AtomicActor {
    
    /** Constructor. Creates ports
     *  @exception NameDuplicationException is thrown if more than one port 
     *  with the same name is added to the actor or if another actor with an
     *  an identical name already exists.
     */
    public RandomValueSource(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _output = new IOPort(this, "output", false, true);
        _delay = new Parameter(this, "delayTime", new DoubleToken(1.0));
    }
    

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Reads one block of data from file and writes it to output port.
     *  Assuming data in 16 bit, higher byte first format.
     */
    public void fire() throws IllegalActionException {
        _output.broadcast(new DoubleToken(Math.random()));
        Director dir = getDirector();
        dir.fireAt(this, dir.getCurrentTime() + 
                ((DoubleToken)_delay.getToken()).doubleValue());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    private IOPort _output;
    private Parameter _delay;
}




