/* This interleaves elements from it's different streams into one stream

 Copyright (c) 1997-2000 The Regents of the University of California.
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

package ptolemy.domains.pn.demo.Interleave;
import ptolemy.domains.pn.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// PNInterleave
/**
Merges it's input streams into one output stream by alternating/circulating
between it's inputs and directing them to the output.

@author Mudit Goel
@version $Id$
*/
public class PNInterleave extends AtomicActor{

    /** Constructor Adds ports to the star
     *  @param container This is the compositeActor containing this actor
     *  @param name This is the name of this actor.
     *  @exception NameDuplicationException This indicates that an actor
     *  with an identical name already exists in the container.
     *  @exception IllegalActionException This can be thrown by one of the
     *  called methods.
     */
    public PNInterleave(CompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _input = new IOPort(this, "input", true, false);
        _input.setMultiport(true);
        _output = new IOPort(this, "output", false, true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** This reads tokens from each of its inputs in a circular fashion and
     *  redirects them each to the output
     *  @exception IllegalActionException This can be thrown by a called
     *  method
     */
    public void fire() throws IllegalActionException {
        Token data;
	while (true) {
	    int width = _input.getWidth();
	    for (int i = 0; i < width; i++) {
		data = _input.get(i);
		_output.broadcast(data);
                System.out.println("Interleave writes " +
                        ((IntToken)data).intValue());
	    }
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Input ports
    private IOPort _input;
    // Output port
    private IOPort _output;

}
