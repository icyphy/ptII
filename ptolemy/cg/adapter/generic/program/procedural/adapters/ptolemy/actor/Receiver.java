/* Code generator adapter for Receiver.

 Copyright (c) 2005-2009 The Regents of the University of California.
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

import ptolemy.actor.IOPort;
import ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////////
////Receiver

/** The base class adapter for recevier.
 *  @author Jia Zou, Man-Kit Leung, Isaac Liu, Bert Rodiers
 *  @version $Id$
 *  @since Ptolemy II 7.1
 *  @Pt.ProposedRating Red (jiazou)
 *  @Pt.AcceptedRating Red (jiazou)
 *
 */
public abstract class Receiver extends ProgramCodeGeneratorAdapter {

    /** Construct the receiver.
     */
    public Receiver(ptolemy.actor.Receiver receiver)
            throws IllegalActionException {
        super(null);

        IOPort port = getComponent().getContainer();
        int channel = port.getChannelForReceiver(getComponent());
        _name = getCodeGenerator().generateVariableName(port) + "_" + channel;
    }

    /** Abstract class to generate code for getting tokens from the receiver.
     *  @return generate get code.
     *  @throws IllegalActionException
     */
    abstract public String generateGetCode() throws IllegalActionException;

    /** Abstract class to generate code to check if the receiver has token.
     *  @return generate hasToken code.
     *  @throws IllegalActionException
     */
    abstract public String generateHasTokenCode() throws IllegalActionException;
    
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
    
    /** Abstract class to generate code for putting tokens from the receiver.
     *  @return generate put code.
     *  @throws IllegalActionException
     */
    abstract public String generatePutCode(String token)
            throws IllegalActionException;

    /** Get the corresponding component */
    public ptolemy.actor.Receiver getComponent() {
        return (ptolemy.actor.Receiver) _component;
    }

    /** Return the name of this receiver
     */
    public String getName() {
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
    abstract protected String _generateTypeConvertStatement(ProgramCodeGeneratorAdapter.Channel source)
            throws IllegalActionException;
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    protected String _name;

}
