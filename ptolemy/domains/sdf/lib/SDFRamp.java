/* An SDF actor that outputs a sequence with a given step in values.

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
//// SDFRamp
/**
This actor is similar to the actor.lib.Ramp actor, but provides
better performance in the SDF domain.
<p
Produce <i>rate</i> output tokens on each firing where each
sucessive token has a value that is incremented by the specified 
step. This actor is similar in function to actor.lib.Ramp, but this
actorr is optimized to provide better performance in the SDF
domain. In order to get improved performance, the <i>rate</i>
parameter must be set to a value greater than 1. The default rate is
256. Thefirst output and the step value are given by parameters.
The type of the output port and the parameters are DoubleToken.

@author Brian K. Vogel. Based on Ramp, by Yuhong Xiong, Edward A. Lee
@version $Id$
*/
// FIXME: Consider allowing arbitrary types instead of constraining to
// be double token.
public class SDFRamp extends SDFSource {

    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructors, construct
     *  the <i>init</i> and <i>step</i> parameters. Initialize <i>init</i>
     *  to IntToken with value 0, and <i>step</i> to IntToken with value 1.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SDFRamp(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        init = new Parameter(this, "init", new DoubleToken(0));
        step = new Parameter(this, "step", new DoubleToken(1));

	// set the type constraints. Set type equals double for
	// performance reasons.
	output.setTypeEquals(BaseType.DOUBLE);;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The value produced by the ramp on its first iteration.
     *  The default value of this parameter is the double 0.
     */
    public Parameter init;

    /** The amount by which the ramp output is incremented on each iteration.
     *  The default value of this parameter is the double 1. The value of
     *  this parameter is not checked again after initialization, so
     *  changes to this parameter after initialization will have no effect.
     */
    public Parameter step;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Notify the director when type changes in the parameters occur.
     *  This will cause type resolution to be redone at the next opportunity.
     *  It is assumed that type changes in the parameters are implemented
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
     *  base class and then sets the <code>init</code> and <code>step</code>
     *  public members to the parameters of the new actor.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @throws CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
        SDFRamp newobj = (SDFRamp)super.clone(ws);
        newobj.init = (Parameter)newobj.getAttribute("init");
        newobj.step = (Parameter)newobj.getAttribute("step");
	// set the type constraints.
	newobj.output.setTypeAtLeast(newobj.init);
	newobj.output.setTypeAtLeast(newobj.step);
        return newobj;
    }

    /** Output <i>rate</i> DoubleTokens. Each successive token is
     *  incremented by <i>step</i>.
     */
    public void fire() {
	try {
	    
	    for (int i = 0; i < _rate; i++) {
		// Convert to double[].
		_resultTokenArray[i] = 
		    new DoubleToken(_stateToken);
		_stateToken = _stateToken + _stepSize;
	    }

	    output.sendArray(0, _resultTokenArray);
	} catch (IllegalActionException ex) {
	    // Errors should not occur here...
            throw new InternalErrorException(
                    "fire failed: " + ex.getMessage());
	}
    }

    /** Set the state to equal the value of the <i>init</i> parameter.
     *  Set the step size equal the value of the <i>step</i> parameter.
     *  The value of these parameters will not be checked again until
     *  the next time this method is called.
     *  The state is incremented by the value of the <i>step</i>
     *  parameter on each iteration (in the postfire() method).
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _stateToken = ((DoubleToken)init.getToken()).doubleValue();
	_stepSize = ((DoubleToken)step.getToken()).doubleValue();
	_resultTokenArray = new DoubleToken[_rate];
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private double _stateToken;
    private double _stepSize;
    private DoubleToken[] _resultTokenArray;
}
