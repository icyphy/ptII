/* Receive x10 commands propagating through an x10 network.

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

import java.util.LinkedList;

import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import x10.Command;
import x10.UnitEvent;
import x10.UnitListener;

//////////////////////////////////////////////////////////////////////////
//// Receiver
/** 
 * Listen for X10 commands propagating through an X10 network. When a
 * command is detected, this actor requests a firing by calling
 * fireAtCurrentTime() on its director. On the next firing, it produces
 * a string description of the command. Alternatively, it can be
 * triggered via the trigger port, or fired by the scheduler when
 * it chooses.
 * @author Colin Cochran and Edward A. Lee
 * @version $Id$
 */

public class Receiver extends X10Interface {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Receiver(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        
        trigger = new TypedIOPort(this, "trigger", true, false);
        trigger.setMultiport(true);
    }
    

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The trigger port.  The type of this port is undeclared, meaning
     *  that it will resolve to any data type.
     */
    public TypedIOPort trigger = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read at most one input token from each channel of the trigger
     *  input and discard it.  If the trigger input is not connected,
     *  then this method does nothing.  Derived classes should be
     *  sure to call super.fire(), or to consume the trigger input
     *  tokens themselves, so that they aren't left unconsumed.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        for (int i = 0; i < trigger.getWidth(); i++) {
            if (trigger.hasToken(i)) {
                trigger.get(i);
            }
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

	/** Begin listening for X10 commands.
     *  @exception IllegalActionException If the super class throws it. 
     */
	public void initialize() throws IllegalActionException {
		super.initialize();
        _interface.addUnitListener(_listener);
	}
    
    /** Remove the <i>UnitListener</i> from the x10 interface.
     *  @exception IllegalActionException If the super class throws it. 
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        if (_interface != null) {
            _interface.removeUnitListener(_listener);
        }
    }
    
	///////////////////////////////////////////////////////////////////
	////                         protected methods                 ////
    
    /** Remove and then return the first command from the command queue.
     *  @return The first command in the command queue, or null if there
     *   is none. 
     */
    protected Command _getCommand (){
        synchronized (_commandQueue){
            return (Command) _commandQueue.removeFirst();   
        }       
    }
    
    /** Return true if the </i>commandQueue<i> is not empty; return false 
     *  otherwise.
     *  @return True if there is a command in the command queue.
     */
    protected boolean _commandReady (){
        synchronized (_commandQueue){
            if (_commandQueue.size() != 0){
                return(true);
            } else {
                return(false);
            }
        }
    }
    
    /** Return a string description of the command.
     *  @return A string description of the command.
     */
    protected static String _commandToString(Command command) {
        byte function = command.getFunctionByte(); 
        
        String functionString = "UNRECOGNIZED_COMMAND";

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
        
        return commandString;
    }
	
	///////////////////////////////////////////////////////////////////
	////                       private variables                   ////
	
	/** This is a linked list that stores any and all received commands from 
     *  a registered </i>CommandListener<i>. 
	 */
    private LinkedList _commandQueue = new LinkedList();
    
	/** This is the </i>UnitListener<i> that will be listening for commands 
     *  from the x10 network
	 */
    private CommandListener _listener = new CommandListener();

    ///////////////////////////////////////////////////////////////////
    ////                        private inner class                ////
    
   /** This is an implementation of the <i>UnitListener</i> interface. One 
    *  callback function exists for each instruction. Refer to the x10 API
    *  for additional information concerning the <i>UntiListener</i> class:
    *  <a href="http://x10.homelinux.org/docs/"> 
    *  http://x10.homelinux.org/docs/</a>
    */
    private class CommandListener implements UnitListener {

        ///////////////////////////////////////////////////////////////////
        ///                       public methods                       ////
        
        public void allLightsOff(UnitEvent event){
            _appendCommand(event);
        }
        public void allLightsOn(UnitEvent event){
            _appendCommand(event);
        }
        public void allUnitsOff(UnitEvent event){
            _appendCommand(event);
        }
        public void unitBright(UnitEvent event){
            _appendCommand(event);
        }
        public void unitDim(UnitEvent event){
            _appendCommand(event);
        }
        public void unitOff(UnitEvent event){
            _appendCommand(event);
        }
        public void unitOn(UnitEvent event){
            _appendCommand(event); 
        }          
        
        ///////////////////////////////////////////////////////////////////
        ///                       private methods                      ////
    
       /** Append a received command onto the </i>commandQueue<i>.
         * @return void
         */
        private void _appendCommand (UnitEvent event){
            if (_debugging) {
                _debug("Detected X10 command: " + _commandToString(event.getCommand()));
            }
            synchronized (_commandQueue){
                _commandQueue.addLast(event.getCommand());
            }
            try {
                // FIXME: Should offer alternative semantics, like blocking.
                getDirector().fireAtCurrentTime(Receiver.this);
            } catch (IllegalActionException ex) {
                throw new RuntimeException("fireAtCurrentTime() "
                        + "threw an exception", ex);
            }       
        }    
    }
}
