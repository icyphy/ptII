/* An actor that converts a boolean token into any other data type.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.conversions;

import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// BooleanToAnything

/**
 <p>This actor converts a boolean input token into any data type.</p>
 <p>A <i>true</i> at the input results in an output with value given
 by the <i>trueValue</i> parameter.
 A <i>false</i> at the input results in an output with value given
 by the <i>falseValue</i> parameter.
 </p>
 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Red (cxh)

 @see ptolemy.data.BooleanToken
 */
public class BooleanToAnything extends Converter {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public BooleanToAnything(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        falseValue = new Parameter(this, "falseValue", new IntToken(0));
        trueValue = new Parameter(this, "trueValue", new IntToken(1));

        input.setTypeEquals(BaseType.BOOLEAN);
        output.setTypeAtLeast(trueValue);
        output.setTypeAtLeast(falseValue);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The value produced at the output when a <i>false</i> input is read. */
    public Parameter falseValue;

    /** The value produced at the output when a <i>true</i> input is read. */
    public Parameter trueValue;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the value public variable in the new
     *  object to equal the cloned parameter in that new object.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        BooleanToAnything newObject = (BooleanToAnything) super
                .clone(workspace);

        // Set the type constraint.
        newObject.output.setTypeAtLeast(newObject.trueValue);
        newObject.output.setTypeAtLeast(newObject.falseValue);
        return newObject;
    }

    /** Read exactly one token from the input and output the token
     *  given by either the <i>falseValue</i> or <i>trueValue</i>
     *  parameter.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        BooleanToken inputToken = (BooleanToken) input.get(0);

        if (inputToken.booleanValue()) {
            output.send(0, trueValue.getToken());
        } else {
            output.send(0, falseValue.getToken());
        }
    }

    /** Return false if the input port has no token, otherwise return
     *  what the superclass returns (presumably true).
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (!input.hasToken(0)) {
            return false;
        }

        return super.prefire();
    }
}
