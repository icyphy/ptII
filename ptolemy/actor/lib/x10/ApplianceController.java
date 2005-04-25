/* An ApplianceController actor sends x10-appliance-module commands to the x10
   network.

   Copyright (c) 2003-2005 The Regents of the University of California.
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
package ptolemy.actor.lib.x10;

import x10.Command;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


//////////////////////////////////////////////////////////////////////////
//// ApplianceController

/**
 * This x10 actor will broadcast appliance-module commands to the X10 network.
 * An appliance module is an X10 device that can turn an appliance on and off.
 * This is a specialized X10 broadcaster actor that will only transmit the
 * following commands:
 * <ul>
 * <li> <b>ON</b>: Turn on an appliance module.
 * <li> <b>OFF</b>: Turn off an appliance module.
 * </ul>
 *@author Colin Cochran (contributor: Edward A. Lee)
 *@version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Green (ptolemy)
 @Pt.AcceptedRating Yellow (ptolemy)
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
        throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Create input ports, each one is a multiport.
        on = new TypedIOPort(this, "on", true, false);
        off = new TypedIOPort(this, "off", true, false);

        // Create attributes that force the names to be shown.
        (new SingletonParameter(on, "_showName")).setToken(BooleanToken.TRUE);
        (new SingletonParameter(off, "_showName")).setToken(BooleanToken.TRUE);

        // Will output true if movement is detected.
        on.setTypeEquals(BaseType.BOOLEAN);
        on.setMultiport(true);
        off.setTypeEquals(BaseType.BOOLEAN);
        off.setMultiport(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** When this port has a true input, the actor will send an ON X10
     *  command. Its type is boolean.
     */
    public TypedIOPort on;

    /** When this port has a true input, the actor will send an OFF X10
     *  command. Its type is boolean.
     */
    public TypedIOPort off;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If a true is received on the <i>on</i> input, then send an ON
     *  command on the X10 network; if a true is received on the <i>off</i>
     *  input, then send an OFF command on the X10 network.  If both are
     *  received, then send the ON before the OFF.
     *  @exception IllegalActionException Is an error occurs reading the inputs.
     */
    public void fire() throws IllegalActionException {
        // Must call super to get command destination.
        super.fire();

        boolean isOn = _hasTrueInput(on);
        boolean isOff = _hasTrueInput(off);

        if (isOn) {
            _transmit(new Command((_destination), x10.Command.ON));
        }

        if (isOff) {
            _transmit(new Command((_destination), x10.Command.OFF));
        }
    }
}
