/* This generates a stream of integer beginning with the seed

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

package ptolemy.domains.pn.demo.Prime;
import ptolemy.domains.pn.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;

//////////////////////////////////////////////////////////////////////////
//// PNRamp
/**

@author Mudit Goel
@version $Id$
*/
public class PNRamp extends AtomicActor {

    /** Constructor Adds ports to the star
     * @param initValue is the initial token that the star puts in the stream
     * @exception NameDuplicationException indicates that an attempt to add
     *  two ports with the same name has been made
     */
    public PNRamp(CompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _output = new IOPort(this, "output", false, true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void setParam(String name, String valueString)
	    throws IllegalActionException {
	if (name.equals("Initial Value")) {
	    IntToken token = new IntToken(valueString);
	    _seed = token.intValue();
	} else {
	    throw new IllegalActionException(this, name +
                    " param does not exist");
	}
    }

    /** Writes successive integers to the output
     */
    public void fire() throws IllegalActionException {
	while(_seed <= 100) {
            //while(true) {
            IntToken data = new IntToken(_seed);
	    _output.broadcast(data);
            Director dir = getDirector();
            //FIXME:::
            dir.fireAt(this, dir.getCurrentTime() + 1);
	    _seed++;
	    //System.out.println("Ramp printed "+_seed);
        }
        //System.out.println("seed is : "+_seed);
        _notdone = false;
    }

    public boolean postfire() {
        return _notdone;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean _notdone = true;

    /* This is the initial value that the star puts in the stream */
    private int _seed;
    /* Output port */
    private IOPort _output;
    //private double _delay = 1.0;
}
