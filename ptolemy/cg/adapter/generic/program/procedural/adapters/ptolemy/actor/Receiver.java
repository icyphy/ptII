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
package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor;

import ptolemy.actor.IOPort;
import ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

/** 
 * 
 *  @author Jia Zou, Man-Kit Leung, Isaac Liu
@version $Id$
@since Ptolemy II 7.1
 *
 */
public abstract class Receiver extends ProgramCodeGeneratorAdapter {

    public Receiver(ptolemy.actor.Receiver receiver) 
    throws IllegalActionException {
        super(null);
        
        IOPort port = getReceiver().getContainer();
        int channel = port.getChannelForReceiver(getReceiver());
        _name = getStrategy().generateVariableName(port) + "_" + channel;
    }

    abstract public String generateGetCode() throws IllegalActionException;

    abstract public String generateHasTokenCode() throws IllegalActionException;

    abstract public String generatePutCode(String token) throws IllegalActionException;
    
    public ptolemy.actor.Receiver getReceiver() {
        return _receiver;
    }
    
    public String getName() {
        return _name;
    }
    
    private ptolemy.actor.Receiver _receiver;
    
    protected String _name;
}
