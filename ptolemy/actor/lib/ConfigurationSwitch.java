/* Split a stream into two according to a boolean selector parameter.

 Copyright (c) 1997-2010 The Regents of the University of California.
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

 This is similar to the BooleanSwitch actor.
 */
package ptolemy.actor.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ConfigurationSwitch

/**
 Split an input stream onto two output ports depending on a
 boolean selector parameter.  The value of the <i>selector</i> parameter specifies the
 output port that should be written to in this and subsequent iterations. If no <i>selector</i>
 parameter value is provided, then the inputs are routed to the <i>falseOutput</i> port.
 In each iteration, at most one token on each channel of the <i>input</i> port
 is read and sent to the corresponding channel of the
 <i>trueOutput</i> port or the <i>falseOutput</i> port, depending on the
 most value of the <i>selector</i> parameter.
 If the input has width greater than an output port, then
 some input tokens will be discarded (those on input channels for which
 there is no corresponding output channel).
 Because tokens are
 immutable, the same Token is sent to the output, rather than a copy.
 The <i>input</i> port may receive Tokens of any type.

 <p>Note that the this actor may be used in Synchronous Dataflow (SDF)
 models, but only under certain circumstances. Specifically, downstream
 actors will be fired whether a token is sent to them or not.
 This will only work if the downstream actors specifically check to
 see whether input tokens are available.

 @author Charles Shelton
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Green (cshelton)
 @Pt.AcceptedRating Red (cshelton)
 */
public class ConfigurationSwitch extends TypedAtomicActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public ConfigurationSwitch(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);

        // Default selector value to false
        selector = new Parameter(this, "selector", new BooleanToken(false));
        selector.setTypeEquals(BaseType.BOOLEAN);

        trueOutput = new TypedIOPort(this, "trueOutput", false, true);
        falseOutput = new TypedIOPort(this, "falseOutput", false, true);
        trueOutput.setTypeAtLeast(input);
        falseOutput.setTypeAtLeast(input);
        trueOutput.setMultiport(true);
        falseOutput.setMultiport(true);
        trueOutput.setWidthEquals(input, true);
        falseOutput.setWidthEquals(input, true);

        /** Make the icon show T and F for trueOutput and falseOutput.
         */
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" " + "width=\"100\" height=\"40\" "
                + "style=\"fill:white\"/>\n" + "<text x=\"39\" y=\"-3\" "
                + "style=\"font-size:14\">\n" + "T \n" + "</text>\n"
                + "<text x=\"39\" y=\"15\" " + "style=\"font-size:14\">\n"
                + "F \n" + "</text>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Parameter that selects one of the two input ports.  The type is
     *  BooleanToken that defaults to false.
     */
    public Parameter selector;

    /** The input port.  The type can be anything. This is a multiport,
     *  and input tokens on all channels are routed to corresponding
     *  channels on the output port, if there are such channels.
     */
    public TypedIOPort input;

    /** Output for tokens on the true path.  The type is at least the
     *  type of the input.
     */
    public TypedIOPort trueOutput;

    /** Output for tokens on the false path.  The type is at least the
     *  type of the input.
     */
    public TypedIOPort falseOutput;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone this actor into the specified workspace. The new actor is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new actor with the same ports as the original, but
     *  no connections and no container.  A container must be set before
     *  much can be done with this actor.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return A new ComponentEntity.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ConfigurationSwitch newObject = (ConfigurationSwitch) super
                .clone(workspace);

        // Set the selector parameter for the cloned actor to the parameter value
        // of the original actor.
        try {
            if (selector != null) {
                if (selector.getToken().equals(BooleanToken.TRUE)) {
                    newObject.selector.setToken(BooleanToken.TRUE);
                } else {
                    newObject.selector.setToken(BooleanToken.FALSE);
                }
            }
        } catch (IllegalActionException ex) {
            throw new CloneNotSupportedException(
                    "Problem with selector parameter "
                            + "in clone method of ConfigurationSelect");
        }

        newObject.trueOutput.setTypeAtLeast(newObject.input);
        newObject.falseOutput.setTypeAtLeast(newObject.input);
        newObject.trueOutput.setWidthEquals(newObject.input, true);
        newObject.falseOutput.setWidthEquals(newObject.input, true);

        return newObject;
    }

    /** Read a token from each input port.  If the token from the
     *  <i>selector</i> input is true, then output the token consumed from the
     *  <i>input</i> port on the <i>trueOutput</i> port,
     *  otherwise output the token on the <i>falseOutput</i> port.
     *
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        // Throw an exception if the selector parameter is null or not
        // a boolean value. This should never happen.
        if (selector == null || selector.getToken() == null
                || !(selector.getToken() instanceof BooleanToken)) {
            throw new IllegalActionException(
                    this,
                    "In ConfigurationSelect actor "
                            + getName()
                            + "the selector parameter must be set to a boolean value.");
        } else {
            for (int i = 0; i < input.getWidth(); i++) {
                if (input.hasToken(i)) {
                    Token token = input.get(i);

                    if (((BooleanToken) selector.getToken()).booleanValue()) {
                        if (i < trueOutput.getWidth()) {
                            trueOutput.send(i, token);
                        }
                    } else {
                        if (i < falseOutput.getWidth()) {
                            falseOutput.send(i, token);
                        }
                    }
                }
            }
        }
    }

    /** Initialize this actor.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
    }
}
