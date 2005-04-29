/* A composite actor whose clock ticks only when enabled.

Copyright (c) 2004-2005 The Regents of the University of California.
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
package ptolemy.domains.sr.lib;

import java.util.Iterator;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sr.kernel.SRDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;


//////////////////////////////////////////////////////////////////////////
//// EnabledComposite

/**
 * This composite actor, designed for use in the SR domain, will have
 * clock ticks only when provided with a true-valued token on the
 * <i>enabled</i> input port. Thus, it can be used to create subclocks
 * of the master clock.
 * <p>
 * Note that this mechanism is a hierarchical version of what is
 * provided by the Lustre and SIGNAL clock calculi, but it
 * circumvents the undecidability of clock signals by requiring
 * the model designer to be explicit about subclocks.
 *
 * @author Edward A. Lee
 * @version $Id$
 @since Ptolemy II 4.1
 * @Pt.ProposedRating Yellow (eal)
 * @Pt.AcceptedRating Red (cxh)
 */
public class EnabledComposite extends TypedCompositeActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public EnabledComposite(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // NOTE: this might be non-strict because it may
        // contain actors that are non-strict.
        new Attribute(this, "_nonStrictMarker");

        enable = new TypedIOPort(this, "enable", true, false);
        enable.setTypeEquals(BaseType.BOOLEAN);

        StringAttribute controlCardinal = new StringAttribute(enable,
                "_cardinal");
        controlCardinal.setExpression("SOUTH");

        Location location = new Location(enable, "_location");
        location.setExpression("[140.0, 35.0]");

        // Create an inside director.
        SRDirector director = new SRDirector(this, "SRDirector");
        location = new Location(director, "_location");
        location.setExpression("[65.0, 35.0]");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The control port for enabling the composite.
     */
    public TypedIOPort enable;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the <i>enable</i> input is not known, then return false;
     *  if it is known and either absent or false,
     *  then produce absent on all the output ports and return false;
     *  if it is known and true, the invoke prefire() on the superclass
     *  and return what it returns.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public boolean prefire() throws IllegalActionException {
        if (!enable.isKnown(0)) {
            // Do nothing, which will leave the outputs unknown.
            if (_debugging) {
                _debug("enabled port status is not known: "
                        + "prefire() returns false.");
            }

            return false;
        } else if (!enable.hasToken(0)
                || !((BooleanToken) enable.get(0)).booleanValue()) {
            // Not enabled. Clear outputs.
            if (_debugging) {
                _debug("Not enabled: prefire() sets all outputs "
                        + "to absent and returns false.");
            }

            Iterator ports = outputPortList().iterator();

            while (ports.hasNext()) {
                IOPort port = (IOPort) ports.next();
                port.broadcastClear();
            }

            return false;
        } else {
            // Actor is enabled. Delegate to the superclass.
            return super.prefire();
        }
    }
}
