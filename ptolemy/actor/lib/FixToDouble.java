/* An actor that convert an input stream containing FixToken types
into a stream of DoubleToken Types.

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

@ProposedRating Red (kienhuis@eecs.berkeley.edu)
@AcceptedRating Red (kienhuis@eecs.berkeley.edu) */

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// FixToDouble
/**
Read a token and converts it to a FixToken with a given precision.

@author Bart Kienhuis 
@version $Id$
*/

public class FixToDouble extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public FixToDouble(TypedCompositeActor container, String name)
	throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        input.setTypeEquals(FixToken.class);
        output.setTypeEquals(DoubleToken.class);

	precision = new Parameter(this, "precision", new StringToken(""));
        precision.setTypeEquals(StringToken.class);      
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The name of the file to write to. This parameter contains
     *  a StringToken.
     */
    public Parameter precision;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read at most one token from each input and convert the Token
     *  value in a FixToken with a given precision.  
     *
     * @exception IllegalActionException If there is no director.  
     */

    public void fire() throws IllegalActionException {
	String _precision = ((StringToken) precision.getToken()).toString();
	// System.out.println(" F->D PRECISION: " + _precision);
	if (input.hasToken(0)) {
    	    FixToken in = (FixToken)input.get(0);
	    DoubleToken result = new DoubleToken(in.doubleValue());    
            output.broadcast(result);
        }
    }

}
