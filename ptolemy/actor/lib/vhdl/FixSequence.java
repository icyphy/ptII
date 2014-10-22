/* A source of a sequence of events.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
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
//// FixSequence

/**
 * This actor produces a sequence of values, optionally periodically repeating
 * them. The <i>values</i> parameter contains an ArrayToken that specifies the
 * sequence of values to produce at the output. If the <i>enable</i> input
 * port is connected, then it provides a boolean sequence that specifies
 * whether to produce an output. If the <i>enable</i> is present and
 * true, then the actor will produce the next output. Otherwise, it
 * produces no output.

 * <p> Compared with the {@link ptolemy.actor.lib.Pulse} actor, this
 * actor can be enabled or disabled on each firing by providing a true
 * or false input on the <i>enable</i> port.
 *
 * @author Terry Esther Filiba, based on Pulse by Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 4.1
 * @see ptolemy.actor.lib.Pulse
 * @Pt.ProposedRating Yellow (eal)
 * @Pt.AcceptedRating Red (cxh)
 */
public class FixSequence extends FixTransformer {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public FixSequence(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // set values parameter
        values = new Parameter(this, "values");
        values.setExpression("{1}");

        // Set the Repeat Flag.
        repeat = new Parameter(this, "repeat", new BooleanToken(false));
        repeat.setTypeEquals(BaseType.BOOLEAN);

        enable = new TypedIOPort(this, "enable", true, false);
        enable.setTypeEquals(BaseType.BOOLEAN);

        trigger = new TypedIOPort(this, "trigger", true, false);
        trigger.setMultiport(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The enable input port.  If this port is connected, then its
     *  input will determine whether an output is produced in any
     *  given firing. The type is boolean.
     */
    public TypedIOPort enable;

    /** The flag that indicates whether the sequence needs to be
     *  repeated. If this is false, then the last value of the
     *  sequence is repeatedly produced after the entire sequence
     *  has been produced. This is a boolean, and defaults to false.
     */
    public Parameter repeat;

    /** The values that will be produced on the output.
     *  This parameter is an array, with default value {1}.
     */
    public Parameter values;

    /** The trigger port.  The type of this port is undeclared, meaning
     *  that it will resolve to any data type.
     */
    public TypedIOPort trigger = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This overrides the
     *  base class to handle type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        FixSequence newObject = (FixSequence) super.clone(workspace);
        newObject.output.setTypeEquals(BaseType.FIX);
        return newObject;
    }

    /** If the <i>enable</i> input is connected, then if it has a true
     *  token, produce the next output. If it is not connected, produce
     *  the next output unconditionally. Whether it is connected is
     *  determined by checking the width of the port.
     *  @exception IllegalActionException If there is no director.
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

        if (!enable.isOutsideConnected() || enable.hasToken(0)
                && ((BooleanToken) enable.get(0)).booleanValue()) {
            ArrayToken valuesArray = (ArrayToken) values.getToken();

            if (_currentIndex < valuesArray.length()) {
                Precision precision = new Precision(
                        ((Parameter) getAttribute("outputPrecision"))
                                .getExpression());

                Overflow overflow = Overflow
                        .getName(((Parameter) getAttribute("outputOverflow"))
                                .getExpression().toLowerCase(
                                        Locale.getDefault()));

                Rounding rounding = Rounding
                        .getName(((Parameter) getAttribute("outputRounding"))
                                .getExpression().toLowerCase(
                                        Locale.getDefault()));

                FixPoint result = new FixPoint(
                        ((ScalarToken) valuesArray.getElement(_currentIndex))
                                .doubleValue(),
                        new FixPointQuantization(precision, overflow, rounding));
                sendOutput(output, 0, new FixToken(result));
                _outputProduced = true;
            }
        }
    }

    /** Initialize the actor by resetting to the first output value.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void initialize() throws IllegalActionException {
        // Note that this will default to null if there is no initialValue set.
        _currentIndex = 0;
        _outputProduced = false;
        super.initialize();
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

    /** Update the state of the actor by moving to the next value
     *  in the <i>values</i> array.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (_outputProduced) {
            _outputProduced = false;
            _currentIndex += 1;

            ArrayToken valuesArray = (ArrayToken) values.getToken();

            if (_currentIndex >= valuesArray.length()) {
                boolean repeatValue = ((BooleanToken) repeat.getToken())
                        .booleanValue();

                if (repeatValue) {
                    _currentIndex = 0;
                } else {
                    // To prevent overflow...
                    _currentIndex = valuesArray.length();
                }
            }
        }

        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The index of the next value to be produced.
    private int _currentIndex;

    // Indicator that an output was produced.
    private boolean _outputProduced;
}
