/* TimeAdvance is a DDE actor that produces DoubleTokens on the output.

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

@ProposedRating Red (davisj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.dde.lib;

import ptolemy.domains.dde.kernel.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.Token;
import ptolemy.data.DoubleToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;


//////////////////////////////////////////////////////////////////////////
//// TimeAdvance
/**
TimeAdvance is a simple DDE actor with an input and output multiport.
When executed, a TimeAdvance will consume a token from its input port
and then produce a DoubleToken on its output port with a value as
specified by the 'outputValue' parameter. TimeAdvance is useful in
conjunction with plotter classes (e.g., actor/gui/TimedPlotter) where
the value to be plotted is not as important as the time axis values.

@author John S. Davis II
@version $Id$

*/

public class TimeAdvance extends DDEActor {

    /** Construct a TimeAdvance actor with the specified container
     *  and name.
     * @param cont The TypedCompositeActor that contains this actor.
     * @param name The name of this actor.
     * @exception NameDuplicationException If the name of this actor
     *  duplicates that of a actor already contained by the container
     *  of this actor.
     * @exception IllegalActionException If there are errors in
     *  instantiating and specifying the type of this actor's ports.
     */
    public TimeAdvance(TypedCompositeActor cont, String name)
            throws IllegalActionException, NameDuplicationException {
        super(cont, name);

        output = new TypedIOPort(this, "output", false, true);
        output.setMultiport(true);
        output.setTypeEquals(BaseType.DOUBLE);
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(BaseType.GENERAL);

        outputValue = new
            Parameter(this, "outputValue", new DoubleToken(0.0));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The output port.
     */
    public TypedIOPort output;

    /** The input port.
     */
    public TypedIOPort input;

    /** A parameter used to specify the value of the DoubleTokens
     * that are produced as outputs of this actor.
     */
    public Parameter outputValue;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute this actor by consuming a token on the input and
     *  producing a DoubleToken on the output with a value specified
     *  by the outputValue parameter.
     * @exception IllegalActionException If there are errors in obtaining
     *  the receivers of this actor.
     */
    public void fire() throws IllegalActionException {
	DoubleToken token = ((DoubleToken)outputValue.getToken());
	Receiver[][] inRcvrs = input.getReceivers();
	if( inRcvrs.length == 0 ) {
	    _continueIterations = false;
	}

	getNextToken();
	output.broadcast(token);
    }

    /** Return true if this actor will allow subsequent iterations to
     *  occur; return false otherwise.
     * @return True if continued execution is enabled; false otherwise.
     * @exception IllegalActionException Is not thrown. It is declared
     *  to allow this exception in derived classes.
     */
    public boolean postfire() throws IllegalActionException {
	return _continueIterations;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean _continueIterations = true;

}
