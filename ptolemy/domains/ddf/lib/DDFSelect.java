/* A type polymorphic select used in the DDF domain.

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
package ptolemy.domains.ddf.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

/**
 A type polymorphic select, which routes specified input channels to
 the output, used in the DDF domain. In the first iteration, an input
 token at the <i>control</i> port is read and its value is recorded.
 In the second iteration, an input token is read from the input port
 channel specified by the most recently seen token at the <i>control</i>
 port and sent to the output. It alternates between these two kinds of
 iterations until stopped. The <i>control</i> port must receive IntTokens.
 The input port may receive tokens of any type. Because tokens are
 immutable, the same token is sent to the output, rather than a copy.
 Note that as for any multiport, the channel number starts from 0 and
 increments by 1 for each additional channel in the order the channel
 is created (e.g., when a connection is drawn in Vergil).
 <p>
 Note this actor sends an output token every two iterations. Contrast
 this with Select which sends an output token every iteration.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Yellow (zgang)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class DDFSelect extends TypedAtomicActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public DDFSelect(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        control = new TypedIOPort(this, "control", true, false);
        control.setTypeEquals(BaseType.INT);
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeAtLeast(input);

        input_tokenConsumptionRate = new Parameter(input,
                "tokenConsumptionRate");
        input_tokenConsumptionRate.setVisibility(Settable.NOT_EDITABLE);
        input_tokenConsumptionRate.setTypeEquals(new ArrayType(BaseType.INT));

        control_tokenConsumptionRate = new Parameter(control,
                "tokenConsumptionRate");
        control_tokenConsumptionRate.setVisibility(Settable.NOT_EDITABLE);
        control_tokenConsumptionRate.setTypeEquals(BaseType.INT);

        // Put the control input on the bottom of the actor.
        StringAttribute controlCardinal = new StringAttribute(control,
                "_cardinal");
        controlCardinal.setExpression("SOUTH");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port.  The port type can be any type.
     */
    public TypedIOPort input;

    /** The input port for control tokens, which specifies the input
     *  channels to read the tokens from.  The type is int.
     */
    public TypedIOPort control;

    /** The output port.  The type is at least the type of <i>input</i>.
     */
    public TypedIOPort output;

    /** This parameter provides token consumption rate for <i>input</i>.
     *  The type is array of ints.
     */
    public Parameter input_tokenConsumptionRate;

    /** This parameter provides token consumption rate for <i>control</i>.
     *  The type is int.
     */
    public Parameter control_tokenConsumptionRate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        DDFSelect newObject = (DDFSelect) super.clone(workspace);
        newObject.output.setTypeAtLeast(newObject.input);
        return newObject;
    }

    /** Pre-calculate the rates to be set in the rate parameter of the
     *  <i>input</i> port. Initialize the private variables _rateZero,
     *  which indicates the <i>input</i> port does not consume any token
     *  from any channel, and _rateArray, each element of which indicates
     *  the <i>input</i> port needs to consume one token from a corresponding
     *  channel and no token from the rest of the channels.
     *  @param port The port that has connection changes.
     */
    @Override
    public void connectionsChanged(Port port) {
        super.connectionsChanged(port);

        if (port == input) {
            try {
                _rateArray = new ArrayToken[input.getWidth()];

                IntToken[] rate = new IntToken[input.getWidth()];

                for (int i = 0; i < input.getWidth(); i++) {
                    rate[i] = _zero;
                }

                _rateZero = new ArrayToken(BaseType.INT, rate);

                for (int i = 0; i < input.getWidth(); i++) {
                    rate[i] = _one;
                    _rateArray[i] = new ArrayToken(rate);
                    rate[i] = _zero;
                }
            } catch (IllegalActionException ex) {
                // shouldn't happen
                throw new InternalErrorException(ex);
            }
        }
    }

    /** Fire the actor once. If the <i>control</i> port is not read in
     *  the previous iteration, read a new token from the <i>control</i>
     *  port and record the value of the token and this concludes the
     *  current firing. Otherwise output the token consumed from the
     *  <i>input</i> port channel specified by the most recently seen
     *  token on the <i>control</i> port.  Then reset an internal variable
     *  so that it will read from the <i>control</i> port in the next
     *  iteration.
     *  @exception IllegalActionException If there is no director, and
     *   hence no receivers have been created, or the value of the received
     *   control token is out of range.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        if (_isControlRead) {
            output.send(0, input.get(_control));
            _isControlRead = false;
        } else {
            _control = ((IntToken) control.get(0)).intValue();

            if (_control >= 0 && _control < input.getWidth()) {
                _isControlRead = true;
            } else {
                // If the value of the received control token is out of
                // range, throw an IllegalActionException.
                throw new IllegalActionException(this, "The width of the "
                        + "input port is " + input.getWidth() + " , but "
                        + "the value of the received control token: "
                        + _control + " is out of range.");
            }
        }
    }

    /** Initialize this actor and rate parameters so that it will read
     *  from the <i>control</i> port in the first iteration.
     *  @exception IllegalActionException If setToken() throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        _isControlRead = false;

        input_tokenConsumptionRate.setToken(_rateZero);
        control_tokenConsumptionRate.setToken(_one);
    }

    /** Update rate parameters for the next iteration.
     *  @return True if execution can continue into the next iteration.
     *  @exception IllegalActionException If setToken() throws it.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (_isControlRead) {
            input_tokenConsumptionRate.setToken(_rateArray[_control]);
            control_tokenConsumptionRate.setToken(_zero);
        } else {
            input_tokenConsumptionRate.setToken(_rateZero);
            control_tokenConsumptionRate.setToken(_one);
        }

        return super.postfire();
    }

    /** Return false if the port or channel it needs to read from in the
     *  following firing does not have a token.
     *  Otherwise, return whatever the superclass returns.
     *  @return True if there are enough tokens to fire.
     *  @exception IllegalActionException If the receivers do not support
     *   the query, or if there is no director, and hence no receivers.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (_isControlRead) {
            if (!input.hasToken(_control)) {
                return false;
            }
        } else {
            if (!control.hasToken(0)) {
                return false;
            }
        }

        return super.prefire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The most recently read <i>control</i> token.
     */
    private int _control;

    /** The boolean to determine whether to read from the <i>control</i>
     *  port or from the <i>input</i> port.
     */
    private boolean _isControlRead;

    /** A final static IntToken with value 1.
     */
    private final static IntToken _one = new IntToken(1);

    /** A final static IntToken with value 0.
     */
    private final static IntToken _zero = new IntToken(0);

    /** An array of ArrayTokens to be used to set tokenConsumptionRate
     *  of the input port. Each ArrayToken indicates the <i>input</i>
     *  port needs to consume one token from a corresponding channel and
     *  no token from the rest of the channels. The array is initialized
     *  in the method connectionsChanged().
     */
    private ArrayToken[] _rateArray;

    /** An ArrayToken to be used to set tokenConsumptionRate of the input
     *  port. It indicates the <i>input</i> port does not consume any token
     *  from any channel. This variable is initialized in the method
     *  connectionsChanged().
     */
    private ArrayToken _rateZero;
}
