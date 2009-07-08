/* Code generator adapter for IOPort.

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
package ptolemy.cg.kernel.generic.program;

import ptolemy.actor.IOPort;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.kernel.util.IllegalActionException;

/** Receiver adapter class.
 * 
 *  @author Jia Zou, Man-Kit Leung, Isaac Liu, Bert Rodiers
 *  @version $Id$
 *  @since Ptolemy II 7.1
 *
 */
public abstract class ReceiverAdapter extends CodeGeneratorAdapter {

    public ReceiverAdapter(ptolemy.actor.Receiver receiver) 
    throws IllegalActionException {
        IOPort port = getReceiver().getContainer();
        int channel = port.getChannelForReceiver(getReceiver());
        _name = getCodeGenerator().generateVariableName(port) + "_" + channel;
    }

    abstract public String generateGetCode() throws IllegalActionException;

    abstract public String generateHasTokenCode() throws IllegalActionException;

    abstract public String generatePutCode(String token) throws IllegalActionException;

    /** Get the code generator associated with this adapter class.
     *  @return The code generator associated with this adapter class.
     *  @see #setCodeGenerator(ProgramCodeGenerator)
     */
    public ProgramCodeGenerator getCodeGenerator() {
        return _codeGenerator;
    }

    public ptolemy.actor.Receiver getReceiver() {
        return _receiver;
    }

    public String getName() {
        return _name;
    }

    /** Set the code generator associated with this adapter class.
     *  @param codeGenerator The code generator associated with this
     *   adapter class.
     *  @see #getCodeGenerator()
     */
    final public void setCodeGenerator(GenericCodeGenerator codeGenerator) {
        _codeGenerator = (ProgramCodeGenerator)codeGenerator;
    }
    
    /** The code generator that contains this adapter class.
     */
    protected ProgramCodeGenerator _codeGenerator;

    /**
     * The code stream associated with this adapter.
     */
    protected CodeStream _codeStream = null;

    protected String _name;

    private ptolemy.actor.Receiver _receiver;

    
}
