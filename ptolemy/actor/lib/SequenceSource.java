/* Base class for sequence-based sources.

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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (bilung@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.graph.Inequality;

//////////////////////////////////////////////////////////////////////////
//// SequenceSource
/**
Base class for sequence sources.  A sequence source is
a source where the output value is logically a sequence, independent
of time, but dependent on the iteration number.  For some time-based
domains, such as CT, actors of this type probably do not make sense
because the number of iterations that the actor experiences per unit
time is not easily determined or controlled.  This actor has a parameter,
<i>firingCountLimit</i>, that optionally limits the number of iterations
for which the actor is fired.  If this number is <i>n</i> > 0, then
the <i>n</i>-th invocation of postfire() returns false, which indicates
to the scheduler that it should stop invocations of this actor.
The default value of <i>firingCountLimit</i>
is zero, which results in postfire always returning
true.  Derived classes must call super.postfire() for this mechanism to
work.

@author Edward A. Lee
@version $Id$
*/

public class SequenceSource extends Source implements SequenceActor {

    /** Construct an actor with the given container and name.
     *  The <i>firingCountLimit</i> parameter is also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SequenceSource(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        firingCountLimit = new Parameter(this, "firingCountLimit",
                new IntToken(0));
	firingCountLimit.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** If greater than zero, then the number of iterations before the
     *  actor indicates to the scheduler that it is finished by returning
     *  false in its postfire() method.
     */
    public Parameter firingCountLimit;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the <code>firingCountLimit</code> public member
     *  to the parameter of the new actor.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
        SequenceSource newobj = (SequenceSource)super.clone(ws);
        newobj.firingCountLimit =
            (Parameter)newobj.getAttribute("firingCountLimit");
        return newobj;
    }

    /** Initialize the iteration counter.  A derived class must call
     *  this method in its initialize() method or the <i>firingCountLimit</i>
     *  feature will not work.
     *  @exception IllegalActionException If the parent class throws it,
     *   which could occur if, for example, the director will not accept
     *   sequence actors.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _iterationCount = 0;
    }

    /** Increment the iteration counter, and if it equals the
     *  value of the <i>iterations</i> parameter, return false.
     *  Otherwise, return true.  Derived classes should call this
     *  at the end of their postfire() method and return its returned
     *  value.
     *  @exception IllegalActionException If firingCountLimit has
     *   an invalid expression.
     */
    public boolean postfire() throws IllegalActionException {
        _iterationCount++;
        if (_iterationCount ==
                ((IntToken)firingCountLimit.getToken()).intValue()) {
            return false;
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _iterationCount = 0;
}
