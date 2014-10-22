/* An actor that implements a control break.

 Copyright (c) 2009-2014 The Regents of the University of California.
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
package ptolemy.domains.sequence.lib;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sequence.kernel.ControlActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

////Break

/**
 <p>An actor that implements a control break.
 If this actor is reached, control returns to the caller.
 This block has no functionality.

  <p>Break is a ControlActor, meaning that it keeps a list of
  enabled output ports.  However, the Break actor has no output ports,
  so this list is always empty here.

  @author Elizabeth Latronico (Bosch)
  @version $Id$
  @since Ptolemy II 10.0
  @Pt.ProposedRating Red (beth)
  @Pt.AcceptedRating Red (beth)
 */

public class Break extends ControlActor {

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public Break(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // set name to invisible
        StringAttribute hideName = new StringAttribute(this, "_hideName");
        hideName.setExpression("true");

        // create inports
        input = new TypedIOPort(this, "input", true, false);

        // Beth added 12/18/08 - Break input is now also a control input
        // Beth changed 02/04/09 - Break input changed back to a regular input
        // This is to be able to connect possibly unsequenced upstream actors
        // which do not necessarily output a boolean
        //input.setControl(true);

        // set portnames to visible
        StringAttribute inputShowName = new StringAttribute(input, "_showName");
        inputShowName.setExpression("false");

        // set direction of ports
        StringAttribute inputCardinal = new StringAttribute(input, "_cardinal");
        inputCardinal.setExpression("WEST");

        // set type constraints for ports
        // The input to the break statement should be a control signal
        // which should be a boolean
        // Beth changed 02/04/09
        // Break input can now be any type
        // This way, can handle Break actors that are introduced because of return
        // ports with sequence numbers.
        // The input to the return port will now also be an input to the Break.
        // This way, the unsequenced actors upstream of the original return port
        // will be sequenced correctly.
        //input.setTypeEquals(BaseType.BOOLEAN);

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input. */
    public TypedIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a check for an unconnected input port.
     *  If the port is unconnected, set the type, so that the type
     *  will not resolve to unknown
     *  Can't use setAtLeast in the constructor, because the input
     *  could be any type, and booleans/integers/reals do not have a
     *  common base type other than unknown
     *
     *  @exception IllegalActionException Not thrown here
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        if (input.connectedPortList().isEmpty()) {
            input.setTypeEquals(BaseType.BOOLEAN);
        }
    }

}
