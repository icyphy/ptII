/* A class that allows to stop a model's execution if a condition returns
   true.

 Copyright (c) 1999-2000 SUPELEC.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL SUPELEC BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE 
 OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF SUPELEC HAS BEEN 
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 SUPELEC SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND SUPELEC HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
 UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY
@ProposedRating Yellow (frederic.boulanger@supelec.fr)
@AcceptedRating Red 
*/

package ptolemy.vergil.debugger;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.expr.*;
import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// Breakpoint
/**
This class implements a breakpoint.  A breakpoint is essentially a boolean
variable that wishes to pause execution when it evaluates to true.  Note that 
this class does not actually halt execution.  Instead, an instance of the
DebugController class inspects for its presence in an actor 
and acts accordingly.

@author SUPELEC team and Steve Neuendorffer
@version $Id$
*/
public class Breakpoint extends Variable {
   
    /** Construct a new breakpoint in the given actor, with the given name.
     * @param actor The actor that contains the breakpoint.
     * @param method The name of the method from executable interface
     * during which the execution will eventually stop.
     * @exception IllegalActionException If the container is not an actor
     * @exception NameDuplicationException If the container already contains
     * an object with the given name.
     */
    public Breakpoint(NamedObj actor, String method) 
	throws IllegalActionException, NameDuplicationException {

      	super(actor, method);
	if(!(actor instanceof Actor))
	    throw new IllegalActionException("Breakpoints can only be " +
					     "contained by Actors");
	
	setExpression("true");
    }

    /////////////////////////////////////////////////////////////////
    //  Public methods

    /** Evaluate the boolean expression of the breakpoint and 
     * returns the boolean value. 
     * @return a boolean true will stop the execution
     */
    public boolean evaluateCondition() {
	boolean returnValue = true;
	try {
	    returnValue = ((BooleanToken)getToken()).booleanValue();
	} catch (IllegalActionException ex) {
	    System.out.println("Can't evaluate condition !!!");
	}
	return returnValue;
    }
}
