/* An actor that does assertion specific for CT domain.

 Copyright (c) 1998-2002 The Regents of the University of California.
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

@ProposedRating Red (hyzheng@eecs.berkeley.edu)
@AcceptedRating Red (hyzheng@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.lib;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.lib.Assertion;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.domains.ct.kernel.CTStepSizeControlActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.*;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// CTAssertion
/**
The CTAssertion actor evaluates an assertion that includes references
to the inputs. The ports are referenced by the variables that have 
the same name as the port. If the assertion is satisfied, nothing 
happens. If not, an exception will be thrown.
To use this class, instantiate it, then add ports (instances of TypedIOPort).
In vergil, you can add ports by right clicking on the icon and selecting
"Configure Ports".  In MoML you can add ports by just including ports
of class TypedIOPort, set to be inputs, as in the following example:
<p>
<pre>
   &lt;entity name="ctAssertion" class="ptolemy.domain.ct.lib.CTAssertion"&gt;
      &lt;port name="in" class="ptolemy.actor.TypedIOPort"&gt;
          &lt;property name="input"/&gt;
      &lt;/port&gt;
   &lt;/entity&gt;
</pre>
<p>
The <i>assertion</i> parameter specifies an assertion that can
refer to the inputs by name.  By default, the assertion
is empty, and attempting
to execute the actor without setting it triggers an exception.
<p>
The <i>errorTolerance</i> parameter specifies the accuracy of
inputs referenced by the assertion. By default, the errorTolerance
is 1e-4.
<p>
NOTE: There are a number of important things to be pointed out.
First, the errorTolerance adds constraints on the accuracy of the
inputs. An alternative way is to leave the evaluator to handle
the errorTolerance. Which one is better is still under discussion.
Second, the CTAssertion actor is also a CTStepSizeControlActor.

@author Haiyang Zheng
@version $Id$
@since Ptolemy II 2.0
*/

public class CTAssertion extends Assertion implements CTStepSizeControlActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public CTAssertion(CompositeEntity container, String name)
	throws NameDuplicationException, IllegalActionException  {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */

    public Object clone(Workspace workspace)
	throws CloneNotSupportedException {
        CTAssertion newObject = (CTAssertion) super.clone(workspace);
        return newObject;
    }

    /** Consume input tokens.
     *  @exception IllegalActionException If the get() method of IOport
     *  or getToken() method of Variable throws it.
     */
    public void fire() throws IllegalActionException {
	super.fire();
    }

    /** Initialize the actor.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _eventMissed = false;
    }
    
    /** Return true if this step does not violate the assertion.
     *  If the assertion does not hold, reduce the step size half and
     *  return false.
     *  @return True if the assertion holds.
     */
    public boolean isThisStepAccurate() {

	try {
	    BooleanToken result = (BooleanToken) evaluate();
	    
	    if (!result.booleanValue()) {
		
		if (_debugging) {
		    _debug(this.getFullName() + " adjusts the step size");
		}
		
		CTDirector dir = (CTDirector)getDirector();
		_eventMissed = true;

		// The refined step size is half of the previous one.
		_refineStep = 0.5*dir.getCurrentStepSize();

		if (_debugging) _debug(getFullName() +
				       " Former stepsize as " + dir.getCurrentStepSize() + 
				       "\nRefined step at" +  _refineStep);
		return false;
	    }
	    else {
		if (_debugging) {
		    _debug(this.getFullName() + " need not adjust the step size");
		}
	    }		    
	} catch (IllegalActionException e) {
	    // which should not happen
	    return false;
	}
	
	_eventMissed = false;
	return true;
    }

    /** Evaluate the assertion.
     *  @exception IllegalActionException If the super class throws it.
     */
    public boolean postfire() throws IllegalActionException {
	return super.postfire();
    }

    /** Return the maximum Double, since this actor does not predict
     *  step size.
     *  @return java.Double.MAX_VALUE.
     */
    public double predictedStepSize() {
        return java.lang.Double.MAX_VALUE;
    }

    /** Return the refined step size if there is a missed event,
     *  otherwise return the current step size.
     *  @return The refined step size.
     */
    public double refinedStepSize() {
        if (_eventMissed) {
            return _refineStep;
        }
        return ((CTDirector)getDirector()).getCurrentStepSize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // flag indicating if the event detection is enable for this step
    private boolean _enabled;

    // flag for indicating a missed event
    private boolean _eventMissed = false;

    // refined step size.
    private double _refineStep;

}
