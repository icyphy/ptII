/* A constant source for the SDF domain.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating
*/

package ptolemy.domains.sdf.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.graph.Inequality;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.data.type.BaseType;

//////////////////////////////////////////////////////////////////////////
//// SDFConst
/**
Produce a constant output. This actor is functionally equivalent to
the actor.lib.Const actor, but is optimized to provide better
performance in the SDF domain.
<p>
 The value of the
output is that of the token contained by the <i>value</i> parameter,
which by default is a IntToken with value 1. The type of the output
is constrained to be at least the type of <i>value</i>.
<p>
The production rate of this actor is set by the <i>rate</i> parameter,
which has a default value of 256.

@author Brian K. Vogel. Based on code from Const, by Yuhong Xiong, Edward A. Lee
@version $Id$
*/

public class SDFConst extends SDFSource {

    /** Construct a constant source with the given container and name.
     *  Create the <i>value</i> parameter, initialize its value to
     *  the default value of an IntToken with value 1.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SDFConst(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
    	value = new Parameter(this, "value", new DoubleToken(1.0));

	// set the type constraints.
	output.setTypeAtLeast(value);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The value produced by this constant source.
     *  By default, it contains an IntToken with value 1.  If the
     *  type of this token is changed during the execution of a model,
     *  then the director will be asked to redo type resolution. This
     *  value is reread on each call to fire().
     */
    public Parameter value;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Notify the director when a type change in the parameter occurs.
     *  This will cause type resolution to be redone at the next opportunity.
     *  It is assumed that type changes in the parameter are implemented
     *  by the director's change request mechanism, so they are implemented
     *  when it is safe to redo type resolution.
     *  If there is no director, then do nothing.
     */
    public void attributeTypeChanged(Attribute attribute) {
        Director dir = getDirector();
        if (dir != null) {
            dir.invalidateResolvedTypes();
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the value public variable in the new
     *  object to equal the cloned parameter in that new object.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @throws CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
        SDFConst newobj = (SDFConst)super.clone(ws);
        newobj.value = (Parameter)newobj.getAttribute("value");
	// Set the type constraint.
	newobj.output.setTypeAtLeast(newobj.value);
        return newobj;
    }

    /** Read the value of the <i>value</i> parameter and output
     *  <i>rate</i> many tokens with that value.
     *  @exception IllegalActionException If it is thrown by the
     *   send() method sending out the token.
     */
    public void fire() throws IllegalActionException {
        super.fire();
	Token curVal = value.getToken();
	//System.out.println("sdfConst: fire: " + this.getFullName() + " ");
	//System.out.println(curVal.toString());
	for (int i = 0; i < _rate; i++) {
	    _resultTokenArray[i] =
		curVal;
	}
	output.sendArray(0, _resultTokenArray);
    }

    /** Allocate the output token array which is used in fire().
     *  @exception IllegalActionException If the parent class throws it.
     */
     public void initialize() throws IllegalActionException {
        super.initialize();
	_resultTokenArray = new Token[_rate];
	// Set the type of <i>value</i> to be the same as the
	// type of the output port. This is done only to optimize
	// performance. For example, if the output port is of
	// type DoubleToken, and <i>value</i> is initially an
	// IntToken, than the code below will make <i>value</i>
	// a DoubleToken as well. If this optimization is not used,
	// than for the above example, a large amount of time will
	// be spent converting types.
	// This assumes that the type of the output is set at some
	// point before this method is called.
	value.setTypeEquals(output.getType());
     }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////


    Token[] _resultTokenArray;
}
