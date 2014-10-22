/* A constant source.

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
package ptolemy.actor.lib.vhdl;

import java.util.Locale;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.FixToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.math.FixPoint;
import ptolemy.math.FixPointQuantization;
import ptolemy.math.Overflow;
import ptolemy.math.Precision;
import ptolemy.math.Rounding;

///////////////////////////////////////////////////////////////////
//// FixConst

/**
 Produce a fix point constant output. The value of the
 output is that of the token contained by the <i>value</i> parameter,
 which by default is an IntToken with value 0. The precision of the fix
 point value is specified by the precision parameter, which is "1e0" by
 default.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class FixConst extends FixTransformer {
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
    public FixConst(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        value = new Parameter(this, "value");
        value.setTypeEquals(BaseType.SCALAR);
        value.setExpression("0.0");

        trigger = new TypedIOPort(this, "trigger", true, false);
        trigger.setMultiport(true);

        // Set the type constraint.
        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"0\" y=\"0\" "
                + "width=\"60\" height=\"20\" " + "style=\"fill:white\"/>\n"
                + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The value produced by this constant source.
     *  By default, it contains an IntToken with value 1.  If the
     *  type of this token is changed during the execution of a model,
     *  then the director will be asked to redo type resolution.
     */
    public Parameter value;

    /** The trigger port.  The type of this port is undeclared, meaning
     *  that it will resolve to any data type.
     */
    public TypedIOPort trigger = null;

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
        FixConst newObject = (FixConst) super.clone(workspace);
        newObject.output.setTypeEquals(BaseType.FIX);
        return newObject;
    }

    /** Send the token in the <i>value</i> parameter to the output.
     *  @exception IllegalActionException If it is thrown by the
     *   send() method sending out the token.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        // NOTE: It might seem that using trigger.numberOfSources() is
        // correct here, but it is not. It is possible for channels
        // to be connected, for example, to other output ports or
        // even back to this same trigger port, in which case higher
        // numbered channels will not have their inputs read.
        for (int i = 0; i < trigger.getWidth(); i++) {
            if (trigger.hasToken(i)) {
                trigger.get(i);
            }
        }

        Precision precision = new Precision(
                ((Parameter) getAttribute("outputPrecision")).getExpression());

        Overflow overflow = Overflow
                .getName(((Parameter) getAttribute("outputOverflow"))
                        .getExpression().toLowerCase(Locale.getDefault()));

        Rounding rounding = Rounding
                .getName(((Parameter) getAttribute("outputRounding"))
                        .getExpression().toLowerCase(Locale.getDefault()));

        FixPoint result = new FixPoint(
                ((ScalarToken) value.getToken()).doubleValue(),
                new FixPointQuantization(precision, overflow, rounding));

        sendOutput(output, 0, new FixToken(result));
    }

    /** If the trigger input is connected and it has no input or an unknown
     *  state, then return false. Otherwise, return true.
     *  @return True, unless the trigger input is connected
     *   and has no input.
     *  @exception IllegalActionException If checking the trigger for
     *   a token throws it or if the super class throws it.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (trigger.numberOfSources() > 0) {
            for (int i = 0; i < trigger.getWidth(); i++) {
                if (trigger.isKnown(i) && trigger.hasToken(i)) {
                    return super.prefire();
                }
            }

            if (_debugging) {
                _debug("Called prefire(), which returns false because"
                        + " the trigger port is connected and has no input.");
            }

            return false;
        }

        return super.prefire();
    }
}
