/* Send commands to an x10 network.

 Copyright (c) 2003 The Regents of the University of California.
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

import ptolemy.actor.NoTokenException;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import x10.Command;

//////////////////////////////////////////////////////////////////////////
//// Sender
/** 
Add broadcast functionality to the X10 interface. Derived classes need to
implement logic for sending commands.

@author Colin Cochran (contributor: Edward A. Lee)
@version $Id$
@since Ptolemy II 3.1
*/

public class Sender extends X10Interface {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already
     *  has anactor.lib.x10
     *   actor with this name.
     */
    public Sender(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

		// Create input ports and port parameters.    
    	houseCode = new StringParameter(this, "houseCode");
        unitCode = new StringParameter(this, "unitCode");
        
    	// The default value for the house code is A while the default value
        // for the unit code is 1. This is the normal x10 default address
        // value for most x10 devices.
		houseCode.setExpression("A");
        unitCode.setExpression("1");
    }
	
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    
    /** This string stores the destination-housecode address. */
    public StringParameter houseCode;
    
    /** This string stores the destination-unitcode address. */ 
    public StringParameter unitCode;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the destination and instruction for this command. Since each x10
     * device is specialized, derived classes will define logic for each 
     * command.
     *  @exception IllegalActionException If super class thows exception.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        _destination = houseCode.stringValue() + unitCode.stringValue();
    }
	
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add a command to the x10-interface send-queue. Commands are
     * broadcasted in a FIFO manner. 
     * @param command The command to be sent on the x10 network.
     */
    protected void _transmit(Command command){
        _interface.addCommand(command);
    }
    
    /** Return true if any channel on a given port has a true input; return
     *  false otherwise.
     *  @return true if any channel on a given port has a true input; return
     *  false otherwise.
     */
    protected boolean _hasTrueInput (TypedIOPort port) 
            throws NoTokenException, IllegalActionException{
        
        boolean hasTrue = false;
        
        if (port.getWidth() > 0){
            for (int i = 0; i < port.getWidth(); i++) {
                if (port.hasToken(i)){
                    if (((BooleanToken)port.get(i)).booleanValue() == true) {
                        hasTrue = true;
                    }
                }
            }
        }
           
        return (hasTrue);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected variables                 ////
	
    /** The address of a given module on the x10 network. */
    protected String _destination;
}
