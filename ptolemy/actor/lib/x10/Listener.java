/* This x10 receiver actor receives any and all commands and outputs them as a
string. 

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Green (ptolemy@ptolemy.eecs.berkeley.edu)
@AcceptedRating Yellow (ptolemy@ptolemy.eecs.berkeley.edu)
*/

package ptolemy.actor.lib.x10;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import x10.Command;

//////////////////////////////////////////////////////////////////////////
//// Listener
/** Receive any and all commands from the x10 network and subsequently 
 *  output each one as a string.
 * 
 *  @author Colin Cochran (contributor: Edward A. Lee)
 *  @version $Id$
 */

public class Listener extends Receiver {

	/** Construct an actor with the given container and name.
	 *  @param container The container.
	 *  @param name The name of this actor.
	 *  @exception IllegalActionException If the actor cannot be contained
	 *   by the proposed container.
	 *  @exception NameDuplicationException If the container already has an
	 *   actor with this name.
	 */
	public Listener(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException  {
		super(container, name);

		// Create output port.    
		receivedCommand = new TypedIOPort(this, "receivedCommand", false, true);
        
		// The command received.
		receivedCommand.setTypeEquals(BaseType.STRING);
	}
	
	///////////////////////////////////////////////////////////////////
	////                     ports and parameters                  ////
    
	/** Output the command received as a string.
	 */
	public TypedIOPort receivedCommand;


	///////////////////////////////////////////////////////////////////
	////                         public methods                    ////
    
    /** Output any received command as a string.
	 * @exception IllegalActionException If super class throws and exception.
	 * @exception InterruptedException If the thread is interupted by another.
	 */
	public void fire() throws IllegalActionException {
		super.fire();
        
        // Check if a command is ready. 
        if(_commandReady()){
            Command command = _getCommand();
        
            byte function = command.getFunctionByte(); 
        
            String functionString = "";

            if (function == x10.Command.ALL_LIGHTS_OFF){
                functionString = "ALL_LIGHTS_OFF";
            } else if (function == x10.Command.ALL_LIGHTS_ON){
                functionString = "ALL_LIGHTS_ON";
            } else if (function == x10.Command.ALL_UNITS_OFF){
                functionString = "ALL_UNITS_OFF";
            } else if (function == x10.Command.BRIGHT){
                functionString = "BRIGHT";
            } else if (function == x10.Command.DIM){
                functionString = "DIM";
            } else if (function == x10.Command.OFF){
                functionString = "OFF";
            } else if (function == x10.Command.ON){
                functionString = "ON";
            }
        
            String commandString = "<" + command.getHouseCode() 
                    + command.getUnitCode() + "-" + functionString + "-" 
                    + command.getLevel() + ">";
        
            receivedCommand.send(0, new StringToken(commandString));
        }
        
        // Check the command queue for more commands to send.
        if(_commandReady()){
            try {
                getDirector().fireAtCurrentTime(Listener.this);
            } catch (IllegalActionException ex) {
                throw new RuntimeException("fireAtCurrentTime() "
                        + "threw an exception", ex);
            } 
        }
	}	
}
