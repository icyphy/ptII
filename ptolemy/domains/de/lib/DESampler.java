/* A DE star that performs sampling on input when the "clock" input arrives.

 Copyright (c) 1997- The Regents of the University of California.
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
//// DESampler
/** 
An actor that sample the input at the times given by events on the "clock"
input. The data value of the "clock" input is ignored. If no input is
available at the time of sampling, the latest input is used. If there has been
no input, then a "zero" particle is produced. The exact meaning of zero
depends on the particle type.

@author Lukito Muliadi
@version $Id$
@see Actor
*/
public class DESampler extends AtomicActor {
    /** Construct a DERamp star.
     *  
     * @param value The initial output event value.
     * @param step The step size by which to increase the output event values.
     * @param container The composite actor that this actor belongs too.
     * @param name The name of this actor.
     *
     * @exception NameDuplicationException Other star already had this name
     * @exception IllegalActionException internal problem
     */	
    public DESampler(CompositeActor container, 
            String name) 
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        // create an output port
        output = new DEIOPort(this, "output", false, true);
        // create input ports
        input = new DEIOPort(this, "data input", true, false);
        clock = new DEIOPort(this, "clock input", true, false);
        input.beforePort = clock;
        clock.triggerList.addElement(output);
        
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** If there's an event in the clock input port then produce an event,
     *  otherwise just record the value of the input port.
     * 
     * @exception CloneNotSupportedException Error when cloning event.
     * @exception IllegalActionException Not thrown in this class.
     */	
    public void fire() 
            throws CloneNotSupportedException, IllegalActionException{
        System.out.println("Firing "+
                           description(CLASSNAME|FULLNAME));

        // Get the receivers.
        DEReceiver clockR = (DEReceiver)(clock.getReceivers())[0][0];
        DEReceiver inputR = (DEReceiver)(input.getReceivers())[0][0];
        
        // Check if there's an event in the clock input port.
        if (clockR.hasToken()) {
            DoubleToken clockToken = null;
            try {
                clockToken = (DoubleToken)(clock.get(0));
            } catch (NoSuchItemException e) {
                // Can't occur
                throw new InvalidStateException("Check DESampler.fire()"+
                        " for bug.");
            }
            // If the input also has token then update _lastToken.
            if (inputR.hasToken()) {
                try {
                _lastToken=(DoubleToken)(input.get(0));
                } catch (NoSuchItemException e) {
                    // Can't occur
                    throw new InvalidStateException("Check DESampler.fire()"+
                            " for bug (2)");
                }
            }
                        
            // send the output token via the output port.
            output.broadcast(_lastToken, ((DECQDirector)getDirector()).currentTime());
        } else if (inputR.hasToken()) {
            // Record the token from the input.
            try {
                _lastToken = (DoubleToken)(input.get(0));
            } catch (NoSuchItemException e) {
                // Can't occur.
                throw new IllegalStateException("Check DESampler.fire() "+
                        "for bug (3)");
             
            }
        } else {
            // if both inputs are empty, then the scheduler is wrong.
            throw new InvalidStateException("DESampler.fire(), "+
                    "bad scheduling");  
        }
    
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.

    // the intial token
    private DoubleToken _zeroToken = new DoubleToken(0.0);

    // the last token seen in the input port.
    private DoubleToken _lastToken = _zeroToken;
    
    // the ports.
    public DEIOPort output;
    public DEIOPort input;
    public DEIOPort clock;
}






