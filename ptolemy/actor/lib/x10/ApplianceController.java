/* An ApplianceController actor sends x10-appliance-module commands to the x10
network.

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
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.SingletonAttribute;
import x10.Command;

//////////////////////////////////////////////////////////////////////////
//// ApplianceController
/** This x10 actor will broadcast appliance-module commands to the x10 network.
 * An appliance module is an x10 device that can turn an appliance on and off.
 * This is a specialized x10 broadcaster actor that will only transmit the 
 * following commands:
 *<ul>
 *<li> <b>On</b>: Turn on an appliance module.
 *<li> <b>Off</b>: Turn off an appliance module.
 *</ul>
 *@author Colin Cochran (contributor: Edward A. Lee)
 *@version $Id$
 */

public class ApplianceController extends Sender {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ApplianceController(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
 
        // Create input ports, each one is a multiport.    
        on = new TypedIOPort(this, "on", true, false);
        off = new TypedIOPort(this, "off", true, false);
        
        // Create attributes that force the names to be shown.
        new SingletonAttribute(on, "_showName");
        new SingletonAttribute(off, "_showName");
        
        // Will output true if movement is detected.
        on.setTypeEquals(BaseType.BOOLEAN);
        on.setMultiport(true);
        off.setTypeEquals(BaseType.BOOLEAN);
        off.setMultiport(true);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
       
   /** 
    * When this port has a true input, the actor will send an <i>on</i> 
    * command. Its type is boolean.
    */
    public TypedIOPort on;
    
   /** 
    * When this port has a true input, the actor will send an <i>off</i> 
    * command. Its type is boolean.
    */
    public TypedIOPort off;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Broadcast an appliance-module command on the x10 network.
     *  @exception IllegalActionException Is super throws exception.
     *  @exception RuntimeException If more than one input port is true.
     */
    public void fire() throws IllegalActionException {
        // Must call super to get command destination.
        super.fire();

        boolean isOn = _hasTrueInput(on);
        boolean isOff = _hasTrueInput(off);
            
        if (isOn & ! isOff) {
            _transmit(new Command((_destination), x10.Command.ON));
        } else if (isOff & !isOn) {
            _transmit(new Command((_destination), x10.Command.OFF));
        } else if (! isOn & ! isOff) {
            // Do not send output if no commands are triggered. 
        } else {
            throw new RuntimeException("Appliance Controller: More than one " 
                    + "input port is true.");
        }	
    }
}
