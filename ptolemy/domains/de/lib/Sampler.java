/* An actor that produces a copy of the most recent input each time
 the trigger input receives an event.

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
package ptolemy.domains.de.lib;

import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// Sampler

/**
 Output the most recent input token when the <i>trigger</i> port
 receives a token.  If no token has been received on the <i>input</i>
 port when a token is received on the <i>trigger</i> port, then the
 value of the <i>initialValue</i> parameter is produced.  If, however,
 the <i>initialValue</i> parameter contains no value, then no output is
 produced.  The inputs can be of any token type, but the <i>output</i>
 port is constrained to be of a type at least that of the <i>input</i>
 port and the <i>initialValue</i> parameter (if it has a value).

 <p> Both the <i>input</i> port and the <i>output</i> port are multiports.
 Generally, their widths should match. Otherwise, if the width of the
 <i>input</i> is greater than the width of the <i>output</i>, the extra
 input tokens will not appear on any output, although they will be
 consumed from the input port. If the width of the <i>output</i> is
 greater than that of the <i>input</i>, then the last few channels of
 the <i>output</i> will never emit tokens.

 <p> Note: If the width of the input changes during execution, then the
 most recent inputs are forgotten, as if the execution of the model
 were starting over.

 <p> This actor is similar to the Inhibit actor in that it modifies a
 stream of events based on the presence or absence of events from
 another input.  This actor reacts to the presence of the other event,
 whereas Inhibit reacts to the absence of it.

 <p> This actor is different from the Register actor in that the input
 tokens are consumed from the input ports before the outputs are generated.

 @author Jie Liu, Edward A. Lee, Steve Neuendorffer, Elaine Cheong
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Yellow (eal)
 @see ptolemy.domains.de.lib.Inhibit
 @see ptolemy.domains.de.lib.Register
 */
public class Sampler extends DETransformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Sampler(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        input.setMultiport(true);
        output.setMultiport(true);
        output.setTypeAtLeast(input);
        trigger = new TypedIOPort(this, "trigger", true, false);

        // Leave type undeclared.
        initialValue = new Parameter(this, "initialValue");

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-30\" y=\"-20\" " + "width=\"60\" height=\"40\" "
                + "style=\"fill:white\"/>\n"
                + "<polyline points=\"0,20 0,0\"/>\n"
                + "<polyline points=\"-30,-0 -10,0 10,-7\"/>\n"
                + "<polyline points=\"10,0 30,0\"/>\n" + "</svg>\n");

        StringAttribute cardinality = new StringAttribute(trigger, "_cardinal");
        cardinality.setExpression("SOUTH");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The trigger port, which has undeclared type. If this port
     *  receives a token, then the most recent token from the
     *  <i>input</i> port will be emitted on the <i>output</i> port.
     */
    public TypedIOPort trigger;

    /** The value that is output when no input has yet been received.
     *  The type should be the same as the input port.
     *  @see #typeConstraintList()
     */
    public Parameter initialValue;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the ports.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Sampler newObject = (Sampler) super.clone(workspace);
        newObject.output.setTypeAtLeast(newObject.input);

        // This is not strictly needed (since it is always recreated
        // in preinitialize) but it is safer.
        newObject._lastInputs = null;

        return newObject;
    }

    /** Consume all the tokens in the input ports and record them.
     *  If there is a token in the <i>trigger</i> port, emit the most
     *  recent token from the <i>input</i> port. If there has been no
     *  input token, but the <i>initialValue</i> parameter has been
     *  set, emit the value of the <i>initialValue</i> parameter.
     *  Otherwise, emit nothing.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        int inputWidth = input.getWidth();
        int outputWidth = output.getWidth();
        int commonWidth = Math.min(inputWidth, outputWidth);

        // If the <i>initialValue</i> parameter was not set, or if the
        // width of the input has changed.
        if ((_lastInputs == null) || (_lastInputs.length != inputWidth)) {
            _lastInputs = new Token[inputWidth];
        }

        // Consume the inputs we save.
        for (int i = 0; i < commonWidth; i++) {
            while (input.hasToken(i)) {
                _lastInputs[i] = input.get(i);
            }
        }

        // Consume the inputs we don't save.
        for (int i = commonWidth; i < inputWidth; i++) {
            while (input.hasToken(i)) {
                input.get(i);
            }
        }

        // If we have a trigger...
        if (trigger.hasToken(0)) {
            // Consume the trigger token.
            trigger.get(0);

            for (int i = 0; i < commonWidth; i++) {
                // Do not output anything if the <i>initialValue</i>
                // parameter was not set and this actor has not
                // received any inputs.
                if (_lastInputs[i] != null) {
                    // Output the most recent token, assuming the
                    // receiver has a FIFO behavior.
                    output.send(i, _lastInputs[i]);
                }
            }
        }
    }

    /** If there is no input on the <i>trigger</i> port, return
     *  false, indicating that this actor does not want to fire.
     *  This has the effect of leaving input values in the input
     *  ports, if there are any.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean prefire() throws IllegalActionException {
        super.prefire();

        // If the trigger input is not connected, never fire.
        if (trigger.getWidth() > 0) {
            return (trigger.hasToken(0));
        } else {
            return false;
        }
    }

    /** Clear the cached input tokens.
     *  @exception IllegalActionException If there is no director.
     */
    public void preinitialize() throws IllegalActionException {
        if (initialValue.getToken() != null) {
            _lastInputs = new Token[input.getWidth()];

            for (int i = 0; i < input.getWidth(); i++) {
                _lastInputs[i] = initialValue.getToken();
            }
        } else {
            _lastInputs = null;
        }

        super.preinitialize();
    }

    /** Override the method in the base class so that the type
     *  constraint for the <i>initialValue</i> parameter will be set
     *  if it contains a value.
     *  @return a list of Inequality objects.
     *  @see ptolemy.graph.Inequality
     */
    public List typeConstraintList() {
        List typeConstraints = super.typeConstraintList();

        try {
            if (initialValue.getToken() != null) {
                // Set type of initialValue to be equal to input type
                Inequality ineq = new Inequality(initialValue.getTypeTerm(),
                        input.getTypeTerm());
                typeConstraints.add(ineq);
                ineq = new Inequality(input.getTypeTerm(), initialValue
                        .getTypeTerm());
                typeConstraints.add(ineq);
            }
        } catch (IllegalActionException ex) {
            // Errors in the initialValue parameter should already
            // have been caught in getAttribute() method of the base
            // class.
            throw new InternalErrorException("Bad initialValue value!");
        }

        return typeConstraints;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The recorded inputs last seen. */
    protected Token[] _lastInputs;
}
