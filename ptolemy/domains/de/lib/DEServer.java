/* A DE star that emulates a server

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
*/

package ptolemy.domains.de.lib;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DEServer
/** 
Emulate a server. If input events arrive when it is not busy, it delays
them by the service time (a constant parameter). If they arrive when it is
not busy, it delays them the service time plus however long it takes to
become free from the previous tasks.

@author Lukito Muliadi
@version $Id$
@see Actor
*/
public class DEServer extends DEActor {
    /** Construct a DEServer star.
     *  
     * @param serviceTime The service time
     * @param container The composite actor that this actor belongs too.
     * @param name The name of this actor.
     *
     * @exception NameDuplicationException Other star already had this name
     * @exception IllegalActionException internal problem
     */	
    public DEServer(double serviceTime, 
            CompositeActor container, 
            String name) 
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        // create an output port
        output = new DEIOPort(this, "output", false, true);
        // create an input port
        input = new DEIOPort(this, "input", true, false);
        // set the service time.
        _serviceTime = serviceTime;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Produce the output event according to whether the server is busy or
     *  not.
     * 
     * @exception CloneNotSupportedException Error when cloning event.
     * @exception IllegalActionException Not thrown in this class.
     */	
    public void fire()
             throws CloneNotSupportedException, IllegalActionException{
        // get the input token from the input port.
        DoubleToken inputToken;
        try {
            inputToken = (DoubleToken)(input.get(0));
        } catch (NoSuchItemException e) {
            // this can't happen
            throw new InvalidStateException("Bug in DEServer.fire()");
        }
        
        // produce the output token.
        double inputTime = ((DECQDirector)getDirector()).getCurrentTime();
	double outputTime;
	if (_firstInput) {
	    _firstInput = false;
	    // always not busy at the first input
	    _doneTime = inputTime + _serviceTime;
	} else {
	    if (_doneTime < inputTime) {
		// not busy
		_doneTime = inputTime + _serviceTime;
	    } else {
		// busy
		_doneTime = _doneTime + _serviceTime;
	    }
	}

	
        // send the output token via output DEIOPort.
        output.broadcast(inputToken, _serviceTime);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.

    // the intial value and increment
    private double _serviceTime;
    private boolean _firstInput = true;
    private double _doneTime = 0.0;
    
    // the ports.
    public DEIOPort output;
    public DEIOPort input;
}






