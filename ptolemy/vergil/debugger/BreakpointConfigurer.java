/* A GUI widget for configuring breakpoints.

 Copyright (c) 1998-2002 The Regents of the University of California.
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

@ProposedRating Red (celaine@eecs.berkeley.edu)
@AcceptedRating Red (celaine@eecs.berkeley.edu)
*/

package ptolemy.vergil.debugger;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.FiringEvent.FiringEventType;
import ptolemy.gui.Query;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.vergil.basic.BasicGraphController;

import javax.swing.BoxLayout;

//////////////////////////////////////////////////////////////////////////
//// BreakpointConfigurer
/**
A GUI widget for configuring breakpoints.  This class is an editor to
configure the breakpoints of an actor.  The user can set breakpoints
before or after any of the following firing events: prefire, fire,
postfire, iterate.

@see ptolemy.actor.gui.PortConfigurer

@author Elaine Cheong
@version $Id$
*/

public class BreakpointConfigurer extends Query {

    /** Construct a breakpoint configurer for the specified entity.
     *  @param object The entity to configure.
     *  @param graphController The associated graph controller for the object.
     */
    public BreakpointConfigurer(Entity object,
            BasicGraphController graphController) {
        super();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setTextWidth(15);

        // Make sure the object we are trying to set a breakpoint on
        // is an Actor.
        if (object instanceof Actor) {
            _actor = (Actor)object;
        } else {
            // This should not be thrown because the context menu
            // (BreakpointDialogFactory) would not have allowed the
            // breakpoint option for non-Actor objects.
            throw new InternalErrorException(
                    "Object selected is not an actor.");
        }

        // Save the GraphController associated with _actor.
        _graphController = graphController;

        // Get the director associated with _actor.
        Director director = ((Actor)_actor).getExecutiveDirector();
        if (director == null) {
            // This should not be thrown because the context menu
            // (BreakpointDialogFactory) would not have allowed the
            // breakpoint option for actors without a director.
            throw new InternalErrorException(
                    "No director associated with this actor.");
        } else {
            // See if the director already has a DebugController.
            DebugController debugController =
                (DebugController) director.getAttribute(_DEBUGCONTROLLER);

            // See if the DebugController has a DebugProfile for this
            // actor.  Make a new DebugProfile if one does not already
            // exist.
            _actorProfile = null;
            if (debugController != null) {
                _actorProfile = debugController.getDebug(_actor);
            }
            if (_actorProfile == null) {
                _actorProfile = new DebugProfile(_graphController);
            }

            // Generate checkbox entries in dialog box.
            for (int i = 0; i < _firingEventTypes.length; i++) {
                if (_actorProfile.isListening(_firingEventTypes[i])) {
                    addCheckBox(_firingEventTypeLabels[i],
                            _firingEventTypeLabels[i], true);
                } else {
                    addCheckBox(_firingEventTypeLabels[i],
                            _firingEventTypeLabels[i], false);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set up and save the new breakpoint configuration for this actor.
     */
    public void apply() {
        boolean breakpointsSelected = false;

        // Make a new DebugProfile.
        DebugProfile profile = new DebugProfile(_graphController);
        for (int i = 0; i < _firingEventTypes.length; i++) {
            // Configure the DebugProfile with the selected FiringEventTypes.
            if (getBooleanValue(_firingEventTypeLabels[i])) {
                profile.listenForEvent(_firingEventTypes[i]);
                breakpointsSelected = true;
            } else {
                profile.unlistenForEvent(_firingEventTypes[i]);
            }
        }

        Director director = ((Actor)_actor).getExecutiveDirector();
        // The director should not be null because we already checked
        // in the constructor.
        DebugController debugController =
            (DebugController) director.getAttribute(_DEBUGCONTROLLER);
            
        // If some breakpoints were selected
        if (breakpointsSelected) {
            // If the director does not already have a
            // DebugController, create one.
            if (debugController == null) {
                try {
                    debugController = new DebugController(director, _DEBUGCONTROLLER);
                } catch (NameDuplicationException exception) {
                    throw new RuntimeException(
                            "Could not create debug controller.");
                } catch (IllegalActionException exception) {
                    throw new RuntimeException(
                            "Could not create debug controller.");
                }
                // Register a new DebugController with the director.
                director.addDebugListener(debugController);

                // FIXME: when do we removeDebugListener?
            } 
            // Add this actor to the set of objects being debugged.
            debugController.setDebug(_actor, profile);
        } else {
            // Remove profile if there are no longer any
            // breakpoints for this _actor.
            debugController.unsetDebug(_actor);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Name of the DebugController to attach to the director.
    private static String _DEBUGCONTROLLER = "_DebugController";
    
    // The object that this configurer configures.
    private Actor _actor;

    // DebugProfile associated with _actor.
    private DebugProfile _actorProfile;
    
    // To add firing events for debugging, you must make changes in 2
    // places in this file: _firingEventTypeLabels, _firingEventTypes
    // Labels of FiringEventTypes to show in the dialog box.
    protected static String[] _firingEventTypeLabels = {
        "before prefire",
        "after prefire",
        "before fire",
        "after fire",
        "before postfire",
        "after postfire",
        "before iterate",
        "after iterate"
    };

    // FiringEventTypes that the user can set breakpoints on.
    protected static FiringEventType[] _firingEventTypes = {
        FiringEvent.BEFORE_PREFIRE,
        FiringEvent.AFTER_PREFIRE,
        FiringEvent.BEFORE_FIRE,
        FiringEvent.AFTER_FIRE,
        FiringEvent.BEFORE_POSTFIRE,
        FiringEvent.AFTER_POSTFIRE,
        FiringEvent.BEFORE_ITERATE,
        FiringEvent.AFTER_ITERATE
    };

    // The GraphController associated with _actor.
    private BasicGraphController _graphController;
}
