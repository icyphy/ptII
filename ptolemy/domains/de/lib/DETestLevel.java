/* A DE star.

 Copyright (c) 1998-1999 The Regents of the University of California.
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
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DETestLevel
/**
Detect threshold crossings if the crossingsOnly parameter is TRUE. Otherwise,
it simply compares the input agains the "threshold".
<p>
If crossingsOnly is TRUE, then: (1) a TRUE is sent to "output" when
the "input" particle exceeds or equals the "threshold" value, having been
previously smaller; (2) a FALSE is sent when the "input" particle is smaller
than "threshold", having been previously larger. Otherwise, no output is
produced.
<p>
If crossingsOnly is FALSE, then a TRUE is sent to "output" whenever any
"input" particle greater than or equal to "threshold" is received, and a
FALSE is sent otherwise.


@author Lukito Muliadi
@version $Id$
@see Actor
*/
public class DETestLevel extends TypedAtomicActor {
    /** Construct a DETestLevel star.
     *
     * @param container The composite actor that this actor belongs too.
     * @param name The name of this actor.
     *
     * @exception NameDuplicationException Other star already had this name
     * @exception IllegalActionException internal problem
     */
    public DETestLevel(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        // create an output port
        output = new TypedIOPort(this, "output", false, true);
        output.setDeclaredType(DoubleToken.class);
        // create an input port
        input = new TypedIOPort(this, "input", true, false);
        output.setDeclaredType(DoubleToken.class);
        
        _crossingsOnly = false;
        _threshold = 0.0;
        _paramTh = new Parameter(this, "Threshold", new DoubleToken(0.0));
	_prev = _threshold;
    }
    /** Construct a DETestLevel star.
     *
     * @param value The initial output event value.
     * @param step The step size by which to increase the output event values.
     * @param container The composite actor that this actor belongs too.
     * @param name The name of this actor.
     *
     * @exception NameDuplicationException Other star already had this name
     * @exception IllegalActionException internal problem
     */
    public DETestLevel(TypedCompositeActor container,
            String name,
            boolean crossingsOnly, double threshold)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        // create an output port
        output = new TypedIOPort(this, "output", false, true);
        output.setDeclaredType(DoubleToken.class);
        // create an input port
        input = new TypedIOPort(this, "input", true, false);
        output.setDeclaredType(DoubleToken.class);
        // set the parameters
        _crossingsOnly = crossingsOnly;
        _threshold = threshold;
        _paramTh = new Parameter(this, "Threshold", 
                new DoubleToken(_threshold));
	_prev = threshold;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Produce the next event at _interval unit-time aparts.
     *
     * @exception IllegalActionException Not thrown in this class.
     */
    public void fire() throws IllegalActionException{
	// get the input token from the input port.
        double inputValue;
        inputValue = ((DoubleToken)(input.get(0))).doubleValue();

        _threshold = ((DoubleToken)_paramTh.getToken()).doubleValue();
        // produce the output token.

	if (_crossingsOnly) {
	    if (_prev <= _threshold && inputValue > _threshold) {
		output.broadcast(new DoubleToken(1.0));
	    } else if (_prev >= _threshold && inputValue < _threshold) {
		output.broadcast(new DoubleToken(-1.0));
	    }
	    _prev = inputValue;

	} else {
	    if (inputValue > _threshold) {
		output.broadcast(new DoubleToken(1.0));
	    } else {
		output.broadcast(new DoubleToken(-1.0));
	    }
	    _prev = inputValue;
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.

    // the intial value and increment
    private double _prev;
    private boolean _crossingsOnly;
    private double _threshold;
    private Parameter _paramTh;

    // the ports.
    public TypedIOPort output;
    public TypedIOPort input;
}










