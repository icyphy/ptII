/* An actor containing a finite state machine (FSM) which is used in
   conjunction with an HDFFSMDirector.

 Copyright (c) 1999-2000 The Regents of the University of California.
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
@AcceptedRating Red (vogel@eecs.berkeley.edu)
*/

package ptolemy.domains.fsm.kernel;

import ptolemy.kernel.CompositeEntity;
import ptolemy.actor.Actor;
import ptolemy.actor.TypedActor;
import ptolemy.actor.sched.Scheduler;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Workspace;
import ptolemy.actor.Director;
import ptolemy.actor.Manager;
import ptolemy.actor.Receiver;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.Token;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Variable;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.Typeable;
import ptolemy.data.type.BaseType;
import ptolemy.graph.Inequality;
import ptolemy.domains.sdf.kernel.*;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// HDFFSMActor
/**
An HDFFSMActor can be used in a modal model to represent the mode control 
logic. A state can have a TypedCompositeActor refinement. This class
must be used instead of FSMActor, if HDFFSMDirector is the local
director.
<p>
An HDFFSMActor contains a set of states and transitions. A transition has
a guard expression. A transition is enabled when its guard expression
is true. A state transition can only occur immediatly following a 
"Type B firing" [1], which is the last firing of the HDF actor in the 
current iteration of the current HDF schedule.
<p>
When a type B firing occurs, the outgoing transitions of the current state
are examined. An IllegalActionException is thrown if there is more than one
enabled transition. If there is exactly one enabled transition then 
the current state of the actor is set to the destination state of the
transition.
<p>
An HDFFSMActor enters its initial state during initialization. The name of the
initial state is specified by the <i>initialStateName</i> parameter.
<p>
An HDFFSMActor contains a set of variables for the input ports that can be
referenced in the guard and trigger expressions of transitions. If an input
port is a single port, one variable is created: an input value variable
with name "<i>portName</i>_V". The input value variable always
contains the latest token received from the port.
If the given port is a multiport, a value variable is
created for each channel. The value variable is named
"<i>portName</i>_<i>channelIndex</i>_V". Note that the syntax for
variables a transition guard expression differs from that used in [1].
The guard expressions allowed here are also currently less expressive
then those in [1]. Particularly, there is currently no token history,
so only the most recently read token can be used in a guard expression.

<H1>References</H1>

<OL>
<LI>
A. Girault, B. Lee, and E. A. Lee, ``<A HREF="http://ptolemy.eecs.berkeley.edu/papers/98/starcharts">Hierarchical
Finite State Machines with Multiple Concurrency Models</A>,'' April 13,
1998.</LI>

@author Brian K. Vogel
@version $Id$
@see State
@see Transition
@see Action
@see HDFFSMDirector
*/
public class HDFFSMActor extends FSMActor implements TypedActor {

    /** Construct an HDFFSMActor in the default workspace with an empty string
     *  as its name. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public HDFFSMActor() {
        super();
    }

    /** Construct an HDFFSMActor in the specified workspace with an empty
     *  string as its name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public HDFFSMActor(Workspace workspace) {
	super(workspace);
    }

    /** Create an HDFFSMActor in the specified container with the specified
     *  name. The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public HDFFSMActor(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the current state of this actor.
     *  @return The current state of this actor.
     */
    public void currentStateSet(State st) {
        _currentState = st;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the last chosen transition.
     *  @return The last chosen transition.
     */
    protected Transition _getLastChosenTransition() {
	return _lastChosenTransition;
    }

}
