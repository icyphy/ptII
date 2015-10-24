/* A director that implements the FMI Master Algorithm Hybrid Co-simulation model of computation. 

   Copyright (c) 2015 The Regents of the University of California.
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
package ptolemy.domains.fmima.kernel;

import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 * A director that implements the FMI Master Algorithm Hybrid Co-simulation model of computation. 
 * @author Fabio Cremona
 * @version $Id: FMUImport.java 73691 2015-10-22 15:12:47Z tsnouidui@lbl.gov $
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class FMIMADirector extends DEDirector {
    /** Construct a director in the default Workspace.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */   
    public FMIMADirector() throws IllegalActionException, NameDuplicationException {
        super();
    }

    /** Construct a director in the given Workspace.
     *  @param workspace The workspace. Container of this director.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public FMIMADirector(Workspace workspace) throws IllegalActionException, NameDuplicationException {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of this director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public FMIMADirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }
    
    /** Fire actors according to events in the event queue. The actual
     *  selecting which events to process is done in _fire(). _fire()
     *  will return whether the previous firing was successful. According
     *  to this information, it is decided whether _fire() should be called
     *  again in order to keep processing events. After each actor firing,
     *  book keeping procedures are called, to keep track of the current
     *  state of the scheduler. The model time of the next events are also
     *  checked to see if we have produced an event of smaller timestamp.
     *  @see #_fire
     *  @exception IllegalActionException If we couldn't process an event
     *  or if an event of smaller timestamp is found within the event queue.
     */
    @Override
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("========= " + this.getName() + " director fires at "
                    + getModelTime() + "  with microstep as " + _microstep);
        }

        // NOTE: This fire method does not call super.fire()
        // because this method is very different from that of the super class.
        // A BIG while loop that handles all events with the same tag.
        while (true) {
            int result = _fire();
            assert result <= 1 && result >= -1;
            if (result == 1) {
                continue;
            } else if (result == -1) {
                _noActorToFire();
                return;
            } // else if 0, keep executing

            // after actor firing, the subclass may wish to perform some book keeping
            // procedures. However in this class the following method does nothing.
            _actorFired();

            if (!_checkForNextEvent()) {
                break;
            } // else keep executing in the current iteration
        } // Close the BIG while loop.

        // Since we are now actually stopping the firing, we can set this false.
        _stopFireRequested = false;

        if (_debugging) {
            _debug("DE director fired!");
        }
    }
}
