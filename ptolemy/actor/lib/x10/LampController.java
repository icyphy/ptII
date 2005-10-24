/* A LampController actor sends X10-light-module commands to the X10 network.

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

import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import x10.Command;

//////////////////////////////////////////////////////////////////////////
//// LampController

/**
 * This x10 actor will broadcast lamp module commands to the X10 network
 * based on the inputs that are provided when it fires.
 * A lamp module is an x10 device that can turn a lamp on and off or control
 * a lamp's brightness level. This is a specialized x10 broadcaster actor
 * that will only transmit the following commands:
 * <ul>
 * <li> <b>Bright</b>: Set a lamp module's brightness level.
 * <li> <b>Dim</b>: Set a lamp module's dimness level.
 * <li> <b>Off</b>: Turn off a lamp module.
 * <li> <b>On</b>: Turn on a lamp module.
 * </ul>
 * @author Colin Cochran and Edward A. Lee
 * @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Yellow (ptolemy)
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
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        bright = new TypedIOPort(this, "bright", true, false);
        dim = new TypedIOPort(this, "dim", true, false);
        on = new TypedIOPort(this, "on", true, false);
        off = new TypedIOPort(this, "off", true, false);

        // Add attributes to indicate that names should be shown.
        (new SingletonParameter(bright, "_showName"))
                .setToken(BooleanToken.TRUE);
        (new SingletonParameter(dim, "_showName")).setToken(BooleanToken.TRUE);
        (new SingletonParameter(on, "_showName")).setToken(BooleanToken.TRUE);
        (new SingletonParameter(off, "_showName")).setToken(BooleanToken.TRUE);

        bright.setTypeEquals(BaseType.INT);
        dim.setTypeEquals(BaseType.INT);
        on.setTypeEquals(BaseType.BOOLEAN);
        on.setMultiport(true);
        off.setTypeEquals(BaseType.BOOLEAN);
        off.setMultiport(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** When provided with an integer between 0 and 100, inclusive,
     *  on this port, this actor will issue an X10 BRIGHT command.
     */
    public TypedIOPort bright;

    /** When provided with an integer between 0 and 100, inclusive,
     *  on this port, this actor will issue an X10 DIM command.
     */
    public TypedIOPort dim;

    /** When provided with a true token on this input port,
     *  this actor will send an ON X10 command.
     */
    public TypedIOPort on;

    /** When provided with a true token on this input port,
     *  this actor will send an OFF X10 command.
     */
    public TypedIOPort off;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Broadcast a light-module command on the X10 network depending
     *  on the input values. If <i>bright</i> or <i>dim</i> provide values
     *  outside the range 0 to 100, inclusive, or if there is no input,
     *  then no BRIGHT or DIM command is issued.  If <i>off</i> or <i>on</i>
     *  are provided with a true input, then issue an OFF or ON command.
     *  Normally it is not useful to provide more than one of these commands
     *  per firing, but this actor will happily issue the commands if that
     *  is what is requested.
     *  @exception IllegalActionException If reading from the ports fails.
     */
    public void fire() throws IllegalActionException {
        // Must call super fire here to get the destination for this command.
        super.fire();

        int brightLevel = -1;
        int dimLevel = -1;

        if ((bright.getWidth() > 0) && bright.hasToken(0)) {
            brightLevel = ((IntToken) bright.get(0)).intValue();
        }

        if ((dim.getWidth() > 0) && dim.hasToken(0)) {
            dimLevel = ((IntToken) dim.get(0)).intValue();
        }

        boolean isOff = _hasTrueInput(off);
        boolean isOn = _hasTrueInput(on);

        if ((brightLevel >= 0) && (brightLevel <= 100)) {
            _transmit(new Command((_destination), x10.Command.BRIGHT,
                    brightLevel));
        }

        if ((dimLevel >= 0) && (dimLevel <= 100)) {
            _transmit(new Command((_destination), x10.Command.DIM, dimLevel));
        }

        if (isOn) {
            _transmit(new Command((_destination), x10.Command.ON));
        }

        if (isOff) {
            _transmit(new Command((_destination), x10.Command.OFF));
        }
    }
}
