/* A type polymorphic boolean select used in the DDF domain.

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
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

/**
 A type polymorphic select with boolean valued control for use in
 the DDF domain. In the first iteration, an input token at the
 <i>control</i> port is read and its value is noted. In the second
 iteration, if the <i>control</i> input read from the previous
 iteration is true, then an input token at the <i>trueInput</i> port
 is read and sent to the output. Likewise with a false <i>control</i>
 input and the <i>falseInput</i> port. It alternates between these
 two kinds of iterations until stopped. The <i>control</i> port must
 receive Boolean Tokens. The <i>trueInput</i> and <i>falseInput</i>
 ports may receive Tokens of any type. Because tokens are immutable,
 the same Token is sent to the output, rather than a copy.
 <p>
 Note this actor sends an output token every two iterations. Contrast
 this with BooleanSelect which sends an output token every iteration.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Yellow (zgang)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class DDFBooleanSelect extends TypedAtomicActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public DDFBooleanSelect(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        trueInput = new TypedIOPort(this, "trueInput", true, false);
        falseInput = new TypedIOPort(this, "falseInput", true, false);
        control = new TypedIOPort(this, "control", true, false);
        control.setTypeEquals(BaseType.BOOLEAN);
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeAtLeast(trueInput);
        output.setTypeAtLeast(falseInput);

        trueInput_tokenConsumptionRate = new Parameter(trueInput,
                "tokenConsumptionRate");
        trueInput_tokenConsumptionRate.setVisibility(Settable.NOT_EDITABLE);
        trueInput_tokenConsumptionRate.setTypeEquals(BaseType.INT);

        falseInput_tokenConsumptionRate = new Parameter(falseInput,
                "tokenConsumptionRate");
        falseInput_tokenConsumptionRate.setVisibility(Settable.NOT_EDITABLE);
        falseInput_tokenConsumptionRate.setTypeEquals(BaseType.INT);

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

    /** Input for tokens on the true path. The port type can be any type.
     */
    public TypedIOPort trueInput;

    /** Input for tokens on the false path. The port type can be any type.
     */
    public TypedIOPort falseInput;

    /** Input that selects one of the other input ports.  The type is
     *  boolean.
     */
    public TypedIOPort control;

    /** The output port.  The type is at least the type of
     *  <i>trueInput</i> and <i>falseInput</i>
     */
    public TypedIOPort output;

    /** This parameter provides token consumption rate for <i>trueInput</i>.
     *  The type is int.
     */
    public Parameter trueInput_tokenConsumptionRate;

    /** This parameter provides token consumption rate for <i>falseInput</i>.
     *  The type is int.
     */
    public Parameter falseInput_tokenConsumptionRate;

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
        DDFBooleanSelect newObject = (DDFBooleanSelect) super.clone(workspace);
        newObject.output.setTypeAtLeast(newObject.trueInput);
        newObject.output.setTypeAtLeast(newObject.falseInput);
        return newObject;
    }

    /** Fire the actor once. If the <i>control</i> port is not read in the
     *  previous iteration, read a new token from the <i>control</i> port
     *  and record the value of the token and this concludes the current
     *  firing. Otherwise output the token consumed from the <i>trueInput</i>
     *  port if the token read from the <i>control</i> port in the previous
     *  firing is true. Likewise with a false <i>control</i> input and the
     *  <i>falseInput</i> port. Then reset an internal variable so that
     *  it will read from the <i>control</i> port in the next iteration.
     *  @exception IllegalActionException If there is no director, and hence
     *   no receivers have been created.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        if (_isControlRead) {
            if (_control) {
                output.send(0, trueInput.get(0));
            } else {
                output.send(0, falseInput.get(0));
            }

            _isControlRead = false;
        } else {
            _control = ((BooleanToken) control.get(0)).booleanValue();
            _isControlRead = true;
        }
    }

    /** Initialize this actor and the rate parameters so that it will read
     *  from the <i>control</i> port in the first iteration.
     *  @exception IllegalActionException If setToken() throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _isControlRead = false;
        trueInput_tokenConsumptionRate.setToken(_zero);
        falseInput_tokenConsumptionRate.setToken(_zero);
        control_tokenConsumptionRate.setToken(_one);
    }

    /** Update rate parameters for the next iteration.
     *  Then return whatever the superclass returns.
     *  @return True if execution can continue into the next iteration.
     *  @exception IllegalActionException If setToken() throws it.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (_isControlRead) {
            if (_control) {
                trueInput_tokenConsumptionRate.setToken(_one);
                falseInput_tokenConsumptionRate.setToken(_zero);
                control_tokenConsumptionRate.setToken(_zero);
            } else {
                trueInput_tokenConsumptionRate.setToken(_zero);
                falseInput_tokenConsumptionRate.setToken(_one);
                control_tokenConsumptionRate.setToken(_zero);
            }
        } else {
            trueInput_tokenConsumptionRate.setToken(_zero);
            falseInput_tokenConsumptionRate.setToken(_zero);
            control_tokenConsumptionRate.setToken(_one);
        }

        return super.postfire();
    }

    /** Return false if the port to read from in the current
     *  iteration does not have a token. If the <i>control</i> port is
     *  not read in the previous iteration, the port to read from
     *  in the current iteration is the <i>control</i> port. Otherwise,
     *  it is the <i>trueInput</i> port or the <i>falseInput</i> port
     *  depending on the <i>control</i> input value read in the
     *  previous iteration.
     *  @return False if there are not enough tokens to fire.
     *  @exception IllegalActionException If the receivers do not support
     *   the query, or if there is no director, and hence no receivers.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (_isControlRead) {
            if (_control) {
                if (!trueInput.hasToken(0)) {
                    return false;
                }
            } else {
                if (!falseInput.hasToken(0)) {
                    return false;
                }
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
    private boolean _control;

    /** The boolean to determine whether to read from the <i>control<i>
     *  port or the <i>trueInput</i>/<i>falseInput</i> port.
     */
    private boolean _isControlRead;

    /** A final static IntToken with value 0.
     */
    private final static IntToken _zero = new IntToken(0);

    /** A final static IntToken with value 1.
     */
    private final static IntToken _one = new IntToken(1);
}
