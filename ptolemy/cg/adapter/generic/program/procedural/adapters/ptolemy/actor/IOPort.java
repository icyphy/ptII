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


import ptolemy.cg.kernel.generic.PortCodeGenerator;
import ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
////IOPort

/**
Code generator adapter for {@link ptolemy.actor.IOPort}.

@author Man-Kit Leung
@version $Id$
@since Ptolemy II 7.1
@Pt.ProposedRating Red (mankit)
@Pt.AcceptedRating Red (mankit)
 */

public class IOPort extends ProgramCodeGeneratorAdapter implements PortCodeGenerator {

    /** Construct the code generator adapter associated
     *  with the given IOPort.
     *  @param component The associated component.
     */
    public IOPort(ptolemy.actor.IOPort component) {
        super(component);
    }

    /////////////////////////////////////////////////////////////////////
    ////                           public methods                    ////

    /** Generate the get code.
     *  @param channel The channel for which the get code is generated.
     *  @return The code that gets data from the channel.
     *  @exception IllegalActionException If the director adapter class cannot be found.
     */
    public String generateGetCode(String channel) throws IllegalActionException {
        Receiver[][] receivers = getReceiverAdapters();
        int channelIndex = Integer.parseInt(channel);
        // FIXME: take care of the offset, and why are we getting all the receivers all the time?
        StringBuffer code = new StringBuffer();
        // FIXME: Don't know why would a channel have more than one relations
        // Thus for now to make sure we don't run into such problems, have a check to ensure
        // this is not true. IF THIS IS TRUE HOWEVER, then the generated code in the receivers would
        // need to change to ensure no name collisions between multiple receivers within the same 
        // channel would occur.
        if (receivers[channelIndex].length > 1) {
            throw new IllegalActionException("Didn't take care of the case where one channel" +
                "has more than one receiver");
        }
        for (int j = 0; j < receivers[channelIndex].length; j++) {
            code.append(receivers[channelIndex][j].generateGetCode());
        }
        return code.toString();
    }
    
    /** Generate the send code.
     *  @param channel The channel for which the send code is generated.
     *  @param dataToken The token to be sent
     *  @return The code that sends the dataToken on the channel.
     *  @exception IllegalActionException If the director adapter class cannot be found.
     */    
    public String generateSendCode(String channel, String dataToken) throws IllegalActionException {
        
        Receiver[][] remoteReceivers = getRemoteReceiverAdapters();
        int channelIndex = Integer.parseInt(channel);
        // FIXME: take care of the offset, and why are we getting all the receivers all the time?
        if ((remoteReceivers == null) || (remoteReceivers.length <= channelIndex)
                || (remoteReceivers[channelIndex] == null)) {
            return "";
        }
        StringBuffer code = new StringBuffer();
        // FIXME: Don't know why would a channel have more than one relations
        // Thus for now to make sure we don't run into such problems, have a check to ensure
        // this is not true. IF THIS IS TRUE HOWEVER, then the generated code in the receivers would
        // need to change to ensure no name collisions between multiple receivers within the same 
        // channel would occur.
        if (remoteReceivers[channelIndex].length > 1) {
            throw new IllegalActionException("Didn't take care of the case where one channel" +
                "has more than one receiver");
        }
        for (int i = 0; i < remoteReceivers[channelIndex].length; i++) {
            Type sourceType = ((ptolemy.actor.TypedIOPort)getComponent()).getType();
            Type sinkType = ((ptolemy.actor.TypedIOPort)remoteReceivers[channelIndex][i].getReceiver().getContainer()).getType();
            dataToken = "$convert_" + getStrategy().codeGenType(sourceType) + "_" 
            + getStrategy().codeGenType(sinkType) + "(" + dataToken + ")";
            code.append(remoteReceivers[channelIndex][i].generatePutCode(dataToken));
        }
        return code.toString();
    }

    /** Generate the initialize code for this director.
     *  The initialize code for the director is generated by appending the
     *  initialize code for each actor.
     *  @return The generated initialize code.
     *  @exception IllegalActionException If illegal macro names are found.
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        ProgramCodeGeneratorAdapter adapter = (ProgramCodeGeneratorAdapter) getCodeGenerator().getAdapter(getComponent().getContainer());

        return adapter.processCode(code.toString());
    }

    public String generateHasTokenCode(String channel)
            throws IllegalActionException {
        Receiver[][] receivers = getReceiverAdapters();
        int channelNumber = Integer.parseInt(channel);
        // FIXME: take care of the offset, and why are we getting all the receivers all the time?
        if (receivers[channelNumber].length > 1) {
            throw new IllegalActionException("Didn't take care of the case where one channel" +
                "has more than one receiver");
        }
        if (receivers[channelNumber].length > 0) {
            return receivers[channelNumber][0].generateHasTokenCode();
        } else {
            return "";
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    /** Get the remote receivers connected to this port.
     * 
     *  @return
     *  @throws IllegalActionException
     */
    public Receiver[][] getRemoteReceiverAdapters() throws IllegalActionException {
        ptolemy.actor.IOPort port = (ptolemy.actor.IOPort) getComponent();
        
        ptolemy.actor.Receiver[][] farReceivers = port.getRemoteReceivers();
        Receiver[][] receiverAdapters = new Receiver[farReceivers.length][];
        for (int i = 0; i < farReceivers.length; i++) {
            receiverAdapters[i] = new Receiver[farReceivers[i].length];
            for (int j = 0; j < farReceivers[i].length; j++) {
                receiverAdapters[i][j] = (Receiver) getAdapter(farReceivers[i][j]);                
            }
        }
        return receiverAdapters;
    }
    
    /** get helpers for receiver
     * @throws IllegalActionException 
     */
    public Receiver[][] getReceiverAdapters() throws IllegalActionException {
        ptolemy.actor.IOPort port = (ptolemy.actor.IOPort) getComponent();
        ptolemy.actor.Receiver[][] receivers = port.getReceivers();
        Receiver[][] receiverAdapters = new Receiver[receivers.length][];
        for (int i = 0; i < receivers.length; i++) {
            receiverAdapters[i] = new Receiver[receivers[i].length];
            for (int j = 0; j < receivers[i].length; j++) {
                receiverAdapters[i][j] = (Receiver) getAdapter(receivers[i][j]);
            }
        }
        return receiverAdapters;
    }


}
