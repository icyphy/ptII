/* Code generator adapter for Receiver.

 Copyright (c) 2009-2014 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.cg.adapter.generic.adapters.ptolemy.actor.Director;
import ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.TemplateParser;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;

///////////////////////////////////////////////////////////////////
//// Receiver

/**
 * The base class adapter for Receiver.
 *
 * @author Jia Zou, Man-Kit Leung, Isaac Liu, Bert Rodiers
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (jiazou)
 * @Pt.AcceptedRating Red (jiazou)
 */
public abstract class Receiver extends ProgramCodeGeneratorAdapter {

    /** Construct the Receiver adapter.
     *  @param receiver The ptolemy.actor.receiver that corresponds
     *  with this adapter
     *  @exception IllegalActionException If thrown by the super class.
     */
    public Receiver(ptolemy.actor.Receiver receiver)
            throws IllegalActionException {
        super(receiver);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Generate code for getting tokens from the receiver.
     * @param offset The offset in the array representation of the port.
     * @return The code for getting tokens from the receiver.
     * @exception IllegalActionException If the receiver adapter is
     * not found or it encounters an error while generating the
     * get code.
     */
    abstract public String generateGetCode(String offset)
            throws IllegalActionException;

    /** Generate code to check if the receiver has a token.
     *  @param offset The offset in the array representation of the port.
     *  @return The code to check if the receiver has a token.
     *  @exception IllegalActionException If an error occurs when
     *  getting the receiver adapters or generating their initialize
     *  code.
     */
    abstract public String generateHasTokenCode(String offset)
            throws IllegalActionException;

    /**
     * Generate the initialize code. In this base class, return empty
     * string. Subclasses may extend this method to generate initialize
     * code of the associated component and append the code to the
     * given string buffer.
     * @return The initialize code of the containing composite actor.
     * @exception IllegalActionException If thrown while appending to the
     * the block or processing the macros.
     */
    public String generateInitializeCode() throws IllegalActionException {
        return _generateBlockByName(_defaultBlocks[1]);
    }

    /**
     * Generate code for putting tokens to the receiver.
     * Note the type conversion is also done in this put method.
     * @param sourcePort The port for which to generate the send code.
     * @param offset The offset in the array representation of the port.
     * @param token The token to be sent.
     * @return The code to put tokens to the receiver.
     * @exception IllegalActionException If the receiver adapter is
     * not found or it encounters an error while generating the send
     * code.
     */
    abstract public String generatePutCode(IOPort sourcePort, String offset,
            String token) throws IllegalActionException;

    /** Get the corresponding component.
     *  @return the component that corresponds with this receiver.
     */
    @Override
    public ptolemy.actor.Receiver getComponent() {
        return (ptolemy.actor.Receiver) _component;
    }

    /** Return the name of this receiver.
     *  @return the name of this receiver.
     */
    @Override
    public String getName() {
        if (_name == null) {
            IOPort port = getComponent().getContainer();
            int channel;
            try {
                channel = port.getChannelForReceiver(getComponent());
            } catch (IllegalActionException e) {
                throw new InvalidStateException(port, e,
                        "Can't retrieve channel for receiver.");
            }
            _name = getCodeGenerator().generateVariableName(port) + "_"
                    + channel;
        }
        return _name;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Generate the type conversion statement for the particular offset of
     * the two given channels. This assumes that the offset is the same for
     * both channel. Advancing the offset of one has to advance the offset of
     * the other.
     * @param source The given source channel.
     * @return The type convert statement for assigning the converted source
     *  variable to the sink variable with the given offset.
     * @exception IllegalActionException If there is a problem getting the
     * adapters for the ports or if the conversion cannot be handled.
     */
    abstract protected String _generateTypeConvertStatement(
            ProgramCodeGeneratorAdapter.Channel source)
            throws IllegalActionException;

    /** Given a String that is an assignment operation, return the
     *  right hand side (the source).
     *  <p>The token should be in the form of "sinkRef = $convert(sourceRef)".</p>
     *  @param token A string that contains the assignment.
     *  @return The right hand side of the assignment.
     *  @exception IllegalActionException  If the token does contain a "=".
     */
    protected String _removeSink(String token) throws IllegalActionException {
        int equalIndex = TemplateParser.indexOf("=", token, 0);

        if (equalIndex < 0) {
            throw new IllegalActionException(
                    getComponent().getContainer(),
                    "The parsed type conversion statement is"
                            + "expected to be of the form: sinkRef = $convert(sourceRef), not \""
                            + token + "\", which does not contain \"=\".");
        }

        return token.substring(equalIndex + 2);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Each receiver is associated with a director, return that director.
     *  @return The director associated with this receiver.
     *  @exception IllegalActionException
     *
     *  FIXME: this is not exactly correct.
     */
    protected Director _getDirectorForReceiver() throws IllegalActionException {
        return (Director) getAdapter(((Actor) getComponent().getContainer()
                .getContainer()).getDirector());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The name of the receiver. */
    private String _name;

}
