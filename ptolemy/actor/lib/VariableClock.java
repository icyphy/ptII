/* A clock source where the period is given as an input.

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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (yuhong@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;

//////////////////////////////////////////////////////////////////////////
//// VariableClock
/**
This actor is identical to Clock except that it has an additional
input port, <i>periodControl</i>.  If this port has a token when the actor
fires, then the value read from that port is used to set the parameter
value.  The initial value of the <i>period</i> parameter is used before
any input is observed on <i>periodControl</i>.
<p>
This actor can be fairly tricky to use with multiple values and
offsets because of the constraint that all offsets must be less
than the period.  Thus, the default values and offsets are different
from those of the base class.  The output value, by default, is
just the constant integer 1, and the offset is 0.0.  The default
value of <i>period</i> is changed to 1.0. This gives
a very simply behavior, where the output is always the same, but
the time between outputs is controlled by the <i>periodControl</i>
input.
<p>
In some domains, such as DE, an input at <i>periodControl</i>
is sufficient to trigger a firing.  This can be somewhat counterintuitive,
since an output from the clock may not be expected at that time.
To avoid this, simple ensure that events at <i>periodControl</i>
arrive only at times when an output is expected.

@author Edward A. Lee
@version $Id$
*/

public class VariableClock extends Clock {

    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public VariableClock(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        periodControl = new TypedIOPort(this, "periodControl", true, false);
        periodControl.setTypeEquals(BaseType.DOUBLE);

        // Change default values from the base class.
        double defaultOffsets[][] = {{0.0}};
        offsets.setToken(new DoubleMatrixToken(defaultOffsets));
        values.setExpression("[1]");
        period.setToken(new DoubleToken(1.0));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The port that controls the value of the <i>period</i> parameter.
     */
    public TypedIOPort periodControl;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the parameter public members to refer
     *  to the parameters of the new actor.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
        VariableClock newobj = (VariableClock)super.clone(ws);
        newobj.periodControl = (TypedIOPort)newobj.getPort("periodControl");
        return newobj;
    }

    /** If there is an input on the <i>periodControl</i> port, read it
     *  and set the value of the <i>period</i> parameter.
     *  Then call the base class fire() method.
     *  @exception IllegalActionException If the input is not positive,
     *   or if the base class throws it.
     */
    public void fire() throws IllegalActionException {
        if(periodControl.getWidth() > 0 && periodControl.hasToken(0)) {
            Token in = periodControl.get(0);
            period.setToken(in);
        }
        super.fire();
    }
}
