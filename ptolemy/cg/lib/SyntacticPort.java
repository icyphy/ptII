/* Syntactic Graph for syntactic representations.

Copyright (c) 2010 The Regents of the University of California.
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

package ptolemy.cg.lib;

import ptolemy.actor.IOPort;
import ptolemy.cg.lib.SyntacticNode;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class SyntacticPort extends ComponentPort {

    /** Construct SyntacticPort. */
    public SyntacticPort() {
        _representedPort = null;
        _isEmpty = true;
    }

    /** Construct SyntacticPort with given workspace.
     *  @param workspace Workspace to add SyntacticPort to.
     */
    public SyntacticPort(Workspace workspace) {
        super(workspace);
        _representedPort = null;
        _isEmpty = true;
    }

    /** Construct SyntacticPort with given container and name.
     *  The caller can associate the syntactic port with an actual port.
     *  The caller must determine if the port is input or output and 
     *  the represented port, if one exist, will be treated inferentially 
     *  as such.
     *  @param container SyntacticNode in which this port is added.
     *  @param port Port referred to by this SyntacticPort.
     *  @param direction True if input, false if output.
     *  @param name Name of this port.
     * */
    public SyntacticPort(SyntacticNode container, Port port, boolean direction, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _representedPort = port;
        _isEmpty = true;
        _isInput = direction;
        
        // If representing an actual port
        if (port != null) {
            if (port instanceof IOPort) {
                IOPort ioport = (IOPort)port;
                _isEmpty = ioport.getWidth() == 0;
                
                
            }
            
            
        }
    }
    
    /** Get the port represented by the Syntactic Port. 
     *  @return represented port.
     *  */
    public Port getRepresentedPort() {
        return _representedPort;
    }
    
    /** Decide whether the port is representationally an input port.
     *  @return whether the port is an input port.
     *  */
    public boolean isInput() {
        return _isInput;
    }
    
    /** Decide whether the port is representationally an output port.
     *  @return whether the port is an output port.
     *  */
    public boolean isOutput() {
        return !_isInput;
    }
    
    /** Decide whether the port is disconnected.
     *  @return whether the port is disconnected.
     *  */
    public boolean isEmpty() {
        return _isEmpty;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    // Port being represented or null if the port is purely Syntactic
    private Port _representedPort;
    
    // Characteristics of represented port
    private boolean _isInput;
    //private boolean _isOutput;
    private boolean _isEmpty;

}
