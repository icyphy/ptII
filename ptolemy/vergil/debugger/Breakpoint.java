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
   This class implements breakpoint. They stop the execution if 
   their condition returns true when evaluated. Their name is 
   the fullname of _myActor appended with ._method. They are stored 
   in a breakpoint list that is implemented by an instance of BrkptList
   and managed by the executive director of _myActor.This class allows 
   to track some parameters values of an actor
@author SUPELEC team
@version $Id$
@see Attribute
@see ptolemy.vergil.debugger.Breakpoint
*/
public class Breakpoint extends Attribute {
   

    /** Constructor
     * @see full-classname#Breakpoint(Nameable actor, String method)
     * @param actor : the actor that contains the breakpoint and on 
     *  the execution will eventually stop.
     * @param method : the name of the method from executable interface
     *  on which the execution will eventually stop.
     * @exception IllegalActionException, NameDuplicatiopnException
     */
    public Breakpoint(Nameable actor, String method) throws IllegalActionException, NameDuplicationException {

      	super((NamedObj) actor, method);
	try {
	    _method = method;
	    _myActor = actor;
	    condition = new Variable((NamedObj)this, "condition");
	    condition.setExpression("true");
	} catch (IllegalActionException ex) {
	    System.out.println("IAE " + ex.getMessage());
	}
	catch (NameDuplicationException ex) {
	    System.out.println("NDE " + ex.getMessage());
	}
	catch (ClassCastException ex) {
	    System.out.println("CCE " + ex.getMessage());
	}
	catch (NullPointerException ex) {
	    System.out.println("NPE " + ex.getMessage());
	}
    }
    //////////////////////////////////////////////////////////////////
    // Public members

    // Contain the boolean expression
    public Variable condition;

    /////////////////////////////////////////////////////////////////
    //  Public methods
    /** Return the container actor
     * @see ptolemy.vergil.debugger.Breakpoint#getActor()
     * @return a Nameable reference on the actor that contains this breakpoint
     *  as an attribute.
     */
    public Nameable getActor() {
	return _myActor;
    }
    
    /** Return a reference on the executive director of _myActor
     * These function is used to edit or modify the breakpoint once
     * it has been had in the executive director's breakpoint list
     * @see ptolemy.vergil.debugger.Breakpoint#getContainerDirector()
     * @return a reference on the executive director of the container actor
     */
    public Nameable getContainerDirector() {
	return ((Actor)_myActor).getExecutiveDirector();
    }

    /** Return the name of the method attached to the breakpoint 
     * @see ptolemy.vergil.debugger.Breakpoint#getMethod()
     * @return the name in a string
     */
    public String getMethod() {
	return _method;
    }

    /** Set the boolean condition that will be evaluated
     * @see ptolemy.vergil.debugger.Breakpoint#setCondition()
     * @param expr the expression to evaluate
     */
    public void setCondition(String expr) {
	condition.setExpression(expr);
    }

    /** Set the method where to stop 
     * @see ptolemy.vergil.debugger.Breakpoint#setMethod()
     * @param newMethod name of the method
     */
    public void setMethod(String newMethod) {
	_method = newMethod;
	try {
	    super.setName(newMethod);
	} catch (IllegalActionException ex) {
	} catch (NameDuplicationException ex) {
	}
    }

    /** Evaluate the boolean expression of the breakpoint and 
     * returns the boolean value. 
     * @see ptolemy.vergil.debugger.Breakpoint#evaluateCondition()
     * @return a boolean true will stop the execution
     */
    public boolean evaluateCondition() {
	boolean returnValue = true;
	try {
	    returnValue = ((BooleanToken)condition.getToken()).booleanValue();
	} catch (IllegalActionException ex) {
	    System.out.println("Can't evaluate condition !!!");
	}
	return returnValue;
    }

    //////////////////////////////////////////////////////////////////
    //     Private members
    private String _method;
    private Nameable _myActor;

}
