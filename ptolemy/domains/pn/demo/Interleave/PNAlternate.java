/* Alternates the input into its different outputs

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
//// PNAlternate
/**
@author Mudit Goel
@version $Id$
*/
public class PNAlternate extends AtomicActor {

    /** Constructor. Creates ports
     * @exception NameDuplicationException is thrown if more than one port
     *  with the same name is added to the star or if another star with an
     *  an identical name already exists.
     */
    public PNAlternate(CompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _input = new IOPort(this, "input", true, false);
	_input.setMultiport(false);
        _output = new IOPort(this, "output", false, true);
        _output.setMultiport(true);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Reads one Token from it's input port and writes this token to
     *  it's output ports. Needs to read one token for every output
     *  port.
     */
    public void fire() throws IllegalActionException {
        Token data;
	int index = 0;
	int width = _output.getWidth();
	while (true) {
	    data = _input.get(0);
	    _output.send(index, data);
	    index = (index+1)%width;
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The input port
    private IOPort _input;
    // The output port
    private IOPort _output;
}
