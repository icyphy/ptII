/* Code generator adapter for IOPort.

 Copyright (c) 2005-2013 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.actor;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
////IOPort

/**
 * Code generator C adapter for {@link ptolemy.actor.IOPort}.
 *
 * @author William Lucas
 * @version $Id$
 * @since Ptolemy II 9.1
 * @Pt.ProposedRating Red (wlc)
 * @Pt.AcceptedRating Red (wlc)
 */

public class IOPort extends ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.IOPort {

    /**
     * Construct the code generator adapter for the given IOPort.
     *
     * @param component
     *            The IOPort.
     */
    public IOPort(ptolemy.actor.IOPort component) {
        super(component);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Generate code for replacing the get() macro.
     *
     * @param channel
     *            The channel for which to generate the get code.
     * @param offset
     *            The offset in the array representation of the port.
     * @return The code that gets data from the specified channel.
     * @exception IllegalActionException
     *                If the receiver adapter is not found or it encounters an
     *                error while generating the get code.
     */
    public String generateGetCode(String channel, String offset)
            throws IllegalActionException {
        
        int channelIndex = Integer.parseInt(channel);
        
        TypedIOPort port = (TypedIOPort) getComponent();
        Type type = port.getType();
        String typeString = getCodeGenerator().codeGenType(type);
        if (!((ptolemy.actor.IOPort)getComponent()).isOutsideConnected())
            return processCode("$new(" + typeString + "(0)).payload." + typeString);
        String result = "(*(" + port.getName() + "->get))((struct IOPort*) " + port.getName() + "_X_COMA_X_ " + channelIndex + ")";
        if (type instanceof BaseType)
            result += ".payload." + typeString;
            
        return result;
    }

    /**
     * Generate code to check if the receiver has a token.
     *
     * @param channel
     *            The channel for which to generate the hasToken code.
     * @param offset
     *            The offset in the array representation of the port.
     * @return The code that checks whether there is data in the specified
     *         channel.
     * @exception IllegalActionException
     *                If the receiver adapter is not found or it encounters an
     *                error while generating the hasToken code.
     */
    public String generateHasTokenCode(String channel, String offset)
            throws IllegalActionException {
        if (!((ptolemy.actor.IOPort)getComponent()).isOutsideConnected())
            return "false";
        int channelNumber = 0;
        channelNumber = Integer.parseInt(channel);
        TypedIOPort port = (TypedIOPort) getComponent();
        String result = "(*(" + port.getName() + "->hasToken))((struct IOPort*) " + port.getName() + ", " + channelNumber + ")";
        
        return result;
    }

    /**
     * Generate code for replacing the send() macro.
     *
     * @param channel
     *            The channel for which to generate the send code.
     * @param offset
     *            The offset in the array representation of the port.
     * @param dataToken
     *            The token to be sent.
     * @return The code that sends data to the specified channel.
     * @exception IllegalActionException
     *                If the receiver adapter is not found or it encounters an
     *                error while generating the send code.
     */
    public String generatePutCode(String channel, String offset,
            String dataToken) throws IllegalActionException {

        if (!((ptolemy.actor.IOPort)getComponent()).isOutsideConnected())
            return "";
        int channelIndex = Integer.parseInt(channel);
        TypedIOPort port = (TypedIOPort) getComponent();
        Type type = port.getType();
        String typeString = getCodeGenerator().codeGenType(type);
        String tokenCode;
        if (type instanceof BaseType)
            tokenCode = "$new(" + typeString + "(" + dataToken + "))";
        else
            tokenCode = dataToken;
        String result = "(*(" + port.getName() + "->send))((struct IOPort*) " 
                + port.getName() + ", " + channelIndex + ", " + tokenCode + ")";
        
        return result;
    }
}
