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

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import x10.Command;
import x10.UnitEvent;
import x10.UnitListener;

//////////////////////////////////////////////////////////////////////////
//// Receiver
/** Receive x10 commands propagating through an x10 network. As x10 
 * commands propagate through an x10 network, the interface device will listen
 * for any activity. If activity is detected, a callback method is invoked by a 
 * listening thread that is constantly blocking and reading the serial port. 
 * This listening thread runs if a <i>UnitListener</i> has been registered to a 
 * <i>Controller</i>. For more information pertaining to UnitListeners, refer 
 * to the x10 API: 
 * <a href="http://x10.homelinux.org/docs/"> http://x10.homelinux.org/docs/</a>
 * 
 * @author Colin Cochran (contributor: Edward A. Lee)
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
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

	/** A <i>UnitListener</i> is registered to the x10 controller to receive
     * commands from the x10 network.
     * @exception IllegalActionException If the super class throws an 
     * exception.
     */
	public void initialize() throws IllegalActionException {
		super.initialize();
        
        _listener = new CommandListener();
                
        _interface.addUnitListener(_listener);
	}
    
    /** Remove the <i>UnitListener</i> from the x10 interface.
     * @exception IllegalActionException If the super class throws an 
     * exception.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        
        _interface.removeUnitListener(_listener);
    }
    
	///////////////////////////////////////////////////////////////////
	////                         protected methods                 ////
    
    /**Remove and then return the first command from the </i>commandQueue<i>.
     * @return Command 
     */
    protected Command _getCommand (){
        
        synchronized (_commandQueue){
            return (Command) _commandQueue.removeFirst();   
        }       
    }
    
    /** Return true if the </i>commandQueue<i> is not empty; return false 
     * otherwise.
     * @return boolean
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
	
	///////////////////////////////////////////////////////////////////
	////                       private variables                   ////
	
	/** This is a linked list that stores any and all received commands from 
     * a registered </i>CommandListener<i>. 
	 */
    private LinkedList _commandQueue = new LinkedList();
    
	/** This is the </i>unitListener<i> that will be listening for commands 
     * from the x10 network
	 */
    private CommandListener _listener;

    
    ///////////////////////////////////////////////////////////////////
    ////                        private inner class                ////
    
   /** This is an implementation of the <i>UnitListener</i> interface. One 
    * callback function exists for each instruction. Refer to the x10 API
    * for additional information concerning the <i>UntiListener</i> class:
    * <a href="http://x10.homelinux.org/docs/"> 
    * http://x10.homelinux.org/docs/</a>
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
        
            synchronized (_commandQueue){
                _commandQueue.addLast(event.getCommand());
            }
    
            try {
               getDirector().fireAtCurrentTime(Receiver.this);
            } catch (IllegalActionException ex) {
                throw new RuntimeException("fireAtCurrentTime() "
                        + "threw an exception", ex);
            }       
        }    
    }
}
