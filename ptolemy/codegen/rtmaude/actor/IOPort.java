/* RTMaude Code generator helper class for the IOPort class.

 Copyright (c) 2009 The Regents of the University of California.
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
package ptolemy.codegen.rtmaude.actor;

import ptolemy.actor.Director;
import ptolemy.codegen.kernel.PortCodeGenerator;
import ptolemy.codegen.rtmaude.kernel.RTMaudeAdaptor;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// IOPort

/**
 * Generate RTMaude code for an IOPort in DE domain.
 *
 * @see ptolemy.actor.IOPort
 * @author Kyungmin Bae
 * @version $Id$
 * @Pt.ProposedRating Red (kquine)
 *
 */
public class IOPort extends RTMaudeAdaptor implements PortCodeGenerator {

    /** Construct the code generator helper associated
     *  with the given IOPort.
     *  @param component The associated component.
     */
    public IOPort(ptolemy.actor.IOPort component) {
        super(component);
    }

    @Override
    public String generateTermCode() throws IllegalActionException {
        ptolemy.actor.IOPort p = (ptolemy.actor.IOPort) getComponent();
        if ( p.getWidth() > 1 )
            return _generateBlockCode("multiBlock");
        else
            return _generateBlockCode(defaultTermBlock,
                    (p.isInput() && p.isOutput() ?
                            "InOut" : (p.isInput() ? "In" : "Out")
                    ) + "Port"
        );
    }

    public String generateCodeForGet(String channel)
            throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }

    public String generateCodeForSend(String channel, String dataToken)
            throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }

    public String generateOffset(String offset, int channel, boolean isWrite,
            Director directorHelper) throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }

    public int getBufferSize(int channelNumber) throws IllegalActionException {
        // TODO Auto-generated method stub
        return 0;
    }

    public Object getReadOffset(int channelNumber)
            throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getWriteOffset(int channelNumber)
            throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }

    public String initializeOffsets() throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }

    public void setBufferSize(int channelNumber, int bufferSize) {
        // TODO Auto-generated method stub

    }

    public void setReadOffset(int channelNumber, Object readOffset) {
        // TODO Auto-generated method stub

    }

    public void setWriteOffset(int channelNumber, Object writeOffset) {
        // TODO Auto-generated method stub

    }

    public String updateConnectedPortsOffset(int rate, Director director)
            throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }

    public String updateOffset(int rate, Director directorHelper)
            throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }

}
