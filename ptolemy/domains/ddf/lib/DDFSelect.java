/* A polymorphic multiplexor with boolean select used in DDF domain.

Copyright (c) 1998-2005 The Regents of the University of California.
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
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;


/**
   A type polymorphic select, which routes specified input channels to
   the output, used in DDF domain. In the first iteration, an input
   token at the <i>control</i> port is read and its value is noted.
   In the second iteration, an input token is read from the input port
   channel specified by the most recently seen token on the <i>control</i>
   port and sent to the output. It alternates between these two kinds of
   iterations until stopped. The <i>control</i> port must receive Int
   Tokens. The input port may receive Tokens of any type. Because tokens
   are immutable, the same Token is sent to the output, rather than a copy.
   <p>
   Note this actor sends an output token every two iterations. Contrast
   this with Select which sends an output token every iteration.

   @author Gang Zhou
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (zgang)
   @Pt.AcceptedRating Red (cxh)
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

        input_tokenConsumptionRate = new Parameter(input, "tokenConsumptionRate");
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

    /** The input port.  The type can be anything.
     */
    public TypedIOPort input;

    /** Input port for control tokens, which specify the input channel
     *  to read token from.  The type is int.
     */
    public TypedIOPort control;

    /** The output port.  The type is at least the type of input.
     */
    public TypedIOPort output;

    /** This parameter provides token consumption rate for input.
     */
    public Parameter input_tokenConsumptionRate;

    /** This parameter provides token consumption rate for control.
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
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        DDFSelect newObject = (DDFSelect) super.clone(workspace);
        newObject.output.setTypeAtLeast(newObject.input);
        return newObject;
    }

    /** Read a new token from the <i>control</i> port and note its value
     *  if it hasn't done so. This concludes the current firing. Otherwise
     *  an input token is read from the input port channel specified by the
     *  most recently seen token on the <i>control</i> port and sent
     *  to the output. Then reset an internal variable so that it will
     *  read from <i>control</i> port in the next iteration.
     *  This method will throw a NoTokenException if any input channel
     *  does not have a token.
     *  @exception IllegalActionException If there is no director, and
     *   hence no receivers have been created.
     */
    public void fire() throws IllegalActionException {
        if (_isControlRead) {
            output.send(0, input.get(_control));
            _isControlRead = false;
        } else {
            _control = ((IntToken) control.get(0)).intValue();

            if ((_control >= 0) && (_control < input.getWidth())) {
                _isControlRead = true;
            } else {
                // If the received control token is out of range,
                // re-read from control port in next iteration.
                _isControlRead = false;
            }
        }
    }

    /** Initialize this actor and rate parameters so that it will read
     *  from the <i>control</i> port in the first iteration.
     *  @exception IllegalActionException If setToken() throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _isControlRead = false;

        Token[] rates = new IntToken[input.getWidth()];

        for (int i = 0; i < input.getWidth(); i++) {
            rates[i] = new IntToken(0);
        }

        input_tokenConsumptionRate.setToken(new ArrayToken(rates));
        control_tokenConsumptionRate.setToken(new IntToken(1));
    }

    /** Update rate parameters for the next iteration.
     *  Then return whatever the superclass returns.
     *  @return True if execution can continue into the next iteration.
     *  @exception IllegalActionException If setToken() throws it.
     */
    public boolean postfire() throws IllegalActionException {
        if (_isControlRead) {
            Token[] rates = new IntToken[input.getWidth()];

            for (int i = 0; i < input.getWidth(); i++) {
                rates[i] = new IntToken(0);
            }

            rates[_control] = new IntToken(1);
            input_tokenConsumptionRate.setToken(new ArrayToken(rates));
            control_tokenConsumptionRate.setToken(new IntToken(0));
        } else {
            Token[] rates = new IntToken[input.getWidth()];

            for (int i = 0; i < input.getWidth(); i++) {
                rates[i] = new IntToken(0);
            }

            input_tokenConsumptionRate.setToken(new ArrayToken(rates));
            control_tokenConsumptionRate.setToken(new IntToken(1));
        }

        return super.postfire();
    }

    /** Return false if the port or channel it needs to read from in the
     *  following firing does not have a token.
     *  Otherwise, return whatever the superclass returns.
     *  @return False if there are not enough tokens to fire.
     *  @exception IllegalActionException If the receivers do not support
     *   the query, or if there is no director, and hence no receivers.
     */
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
    // The most recently read control token.
    private int _control;

    // The boolean to determine whether to read from control port
    // or from input port.
    private boolean _isControlRead;
}
