/* A when operator for the SR domain.

Copyright (c) 1997-2005 The Regents of the University of California.
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

*/
package ptolemy.domains.sr.lib;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;


//////////////////////////////////////////////////////////////////////////
//// When

/**
   When the <i>control</i> input is true, copy the <i>input</i> to the
   <i>output</i>. If <i>control</i> is absent, or it is true and
   <i>input</i> is absent, then the output is absent.
   This actor is inspired by the "when" operator in the synchronous
   languages LUSTRE and SIGNAL.

   @author Edward A. Lee
   @version $Id$
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (cxh)
*/
public class When extends Transformer {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public When(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);

        control = new TypedIOPort(this, "control", true, false);
        control.setTypeEquals(BaseType.BOOLEAN);

        StringAttribute controlCardinal = new StringAttribute(control,
                "_cardinal");
        controlCardinal.setExpression("SOUTH");

        output.setTypeAtLeast(input);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The control input port. This has type boolean.
     */
    public TypedIOPort control;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This overrides the
     *  base class to handle type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        When newObject = (When) super.clone(workspace);
        newObject.output.setTypeAtLeast(newObject.input);
        return newObject;
    }

    /** If the <i>control</i> input is present and true, then copy
     *  the input to the output.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (control.hasToken(0)
                        && ((BooleanToken) control.get(0)).booleanValue()
                        && input.hasToken(0)) {
            output.send(0, input.get(0));
        } else {
            output.sendClear(0);
        }
    }
}
