/* A LampController actor sends x10-light-module commands to the x10 network.

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
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.SingletonAttribute;
import x10.Command;

//////////////////////////////////////////////////////////////////////////
//// LampController
/** This x10 actor will broadcast lamp module commands to the x10 network.
 * A lamp module is an x10 device that can turn a lamp on and off or control 
 * a lamp's brightness level. This is a specialized x10 broadcaster actor 
 * that will only transmit the following commands:
 * <ul>
 * <li> <b>Bright</b>: Increase a lamp module's brightness level.
 * <li> <b>Dim</b>: Decrease a lamp module's brightness level.
 * <li> <b>Off</b>: Turn off a lamp module.
 * <li> <b>On</b>: Turn on a lamp module.
 * </ul>
 * @author Colin Cochran (contributor: Edward A. Lee)
 * @version $Id$
 */

public class LampController extends Sender {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public LampController(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // Create input ports and port parameters.
        lightLevel = new Parameter(this, "lightLevel");
        
        bright = new TypedIOPort(this, "bright", true, false);
        dim = new TypedIOPort(this, "dim", true, false);
        on = new TypedIOPort(this, "on", true, false);
        off = new TypedIOPort(this, "off", true, false);
        
        // Add attributes to indicate that names should be shown.
        new SingletonAttribute(bright, "_showName");
        new SingletonAttribute(dim, "_showName");
        new SingletonAttribute(on, "_showName");
        new SingletonAttribute(off, "_showName");
        
        // lightLevel is the aboslute percentage amount to either dim or 
        // brighten a light. It can be an integer between 1 and 100 
        // inclusively and its default value is 15.
		lightLevel.setExpression("15");
        lightLevel.setTypeEquals(BaseType.INT);
        
        bright.setTypeEquals(BaseType.BOOLEAN);
        bright.setMultiport(true);
        dim.setTypeEquals(BaseType.BOOLEAN);
        dim.setMultiport(true);
        on.setTypeEquals(BaseType.BOOLEAN);
        on.setMultiport(true);
        off.setTypeEquals(BaseType.BOOLEAN);
        off.setMultiport(true);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
     
   /** When true, the actor will send a <i>bright</i> command.
    */
    public TypedIOPort bright;
   
   /** When true, the actor will send a <i>dim</i> command.
    */
    public TypedIOPort dim;  
         
   /** When true, the actor will send an <i>on</i> command.
    */
    public TypedIOPort on;
    
   /** When true, the actor will send an <i>off</i> command.
    */
    public TypedIOPort off;

	/** lightLevel is the absolute percentage amount to either dim or brighten 
	 * a light connected to a lamp module. It is set to a value on a scale from
	 * 1 to 100, inclusively, where 1 would change the brightness level by 1% 
	 * of the lamp module's full power. Its default value is 15 percent of full 
     * power. 
	 */
	public Parameter lightLevel;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Broadcast a light-module command on the x10 network. Only one 
     * instruction-input port may have a <i>true</i> input. All other 
     * instruction-input ports must be false or not have a connection. Each 
     * port is a multiport.
     * @exception IllegalActionException If super class throws.
     * @exception RuntimeException If more than one instruction input port has
     * a <i>true</i> value.
     */
    public void fire() throws IllegalActionException {
        // Must call super fire here to get the destination for this command. 
        super.fire();

		_lightLevel = ((IntToken)lightLevel.getToken()).intValue();

        boolean isBright = _hasTrueInput(bright);
        boolean isDim = _hasTrueInput(dim);
        boolean isOff = _hasTrueInput(off);
        boolean isOn = _hasTrueInput(on);
        
        if (isBright & ! (isDim | isOff | isOn )){
            
            _transmit(new Command((_destination), x10.Command.BRIGHT
                    , _lightLevel));
                    
        } else if (isDim & ! (isBright | isOff | isOn)) {
           
            _transmit(new Command((_destination), x10.Command.DIM
                    , _lightLevel));
                    
        } else if (isOn & ! (isBright | isDim | isOff)) {
            
            _transmit(new Command((_destination), x10.Command.ON));
            
        } else if (isOff & ! (isBright | isDim | isOn)) {
            
            _transmit(new Command((_destination), x10.Command.OFF));
            
        } else if (! (isBright | isDim | isOff | isOn)) {
            // Do not send output if no commands are triggered. 
        } else {
            throw new RuntimeException( "Lamp Controller: More than one input " 
                    + "port is true.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

	/** This is the amount to brighten or dim the lamp module. Its type is
	 * int.
	 */
	protected int _lightLevel;

}
