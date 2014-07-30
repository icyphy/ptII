/* A GUI widget for configuring breakpoints.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.vergil.debugger;

import javax.swing.BoxLayout;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.FiringEvent.FiringEventType;
import ptolemy.gui.Query;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.basic.BasicGraphController;

///////////////////////////////////////////////////////////////////
//// BreakpointConfigurer

/**
 A GUI widget for configuring breakpoints.  This class is an editor to
 configure the breakpoints of an actor in an SDF model.
 The user can set breakpoints before or after iterate().

 <p>There is further documentation in the
 <a href="package-summary.html">package summary</a>.

 @see ptolemy.actor.gui.PortConfigurerDialog

 @author Elaine Cheong, Contributor: Christopher Brooks
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (celaine)
 @Pt.AcceptedRating Red (celaine)
 */
@SuppressWarnings("serial")
public class BreakpointConfigurer extends Query implements ChangeListener {
    /** Construct a breakpoint configurer for the specified entity.
     *  @param object The entity to configure.
     *  @param graphController The associated graph controller for the object.
     */
    public BreakpointConfigurer(Entity object,
            BasicGraphController graphController) {
        super();

        // FIXME: Perhaps this dialog should have a help button?
        // The text in $PTII/doc/coding/debugging.htm could be used.
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setTextWidth(15);

        // Make sure the object we are trying to set a breakpoint on
        // is an Actor.
        if (object instanceof Actor) {
            _actor = (Actor) object;
        } else {
            // This should not be thrown because the context menu
            // (BreakpointDialogFactory) would not have allowed the
            // breakpoint option for non-Actor objects.
            throw new InternalErrorException("Object selected is not an actor.");
        }

        // Save the GraphController associated with _actor.
        _graphController = graphController;

        // Get the director associated with _actor.
        Director director = _actor.getExecutiveDirector();

        if (director == null) {
            // This should not be thrown because the context menu
            // (BreakpointDialogFactory) would not have allowed the
            // breakpoint option for actors without a director.
            throw new InternalErrorException(
                    "No director associated with this actor.");
        } else {
            // FIXME: Currently, it seems that this facility
            // only works in SDF.
            // Perhaps this constructor should throw IllegalActionException.
            if (_sdfDirectorClass == null) {
                try {
                    _sdfDirectorClass = Class
                            .forName("ptolemy.domains.sdf.kernel.SDFDirector");
                } catch (Throwable throwable) {
                    throw new InternalErrorException(
                            object,
                            throwable,
                            "The breakpoint facility only works with "
                                    + "that are instances of SDFDirector.  The "
                                    + "SDFDirector was not found.");
                }
            }

            if (!_sdfDirectorClass.isInstance(director)) {
                throw new InternalErrorException(
                        director,
                        null,
                        "The breakpoint facility only works with directors "
                                + "that are instances of SDFDirector.  The director "
                                + "of this model is a '" + director + "'.");
            }

            // See if the director already has a DebugController.
            DebugController debugController = (DebugController) director
                    .getAttribute(_DEBUGCONTROLLER);

            // See if the DebugController has a DebugProfile for this
            // actor.  Make a new DebugProfile if one does not already
            // exist.
            _actorProfile = null;

            if (debugController != null) {
                _actorProfile = debugController.getDebugProfile(_actor);
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
        _actorProfile = new DebugProfile(_graphController);

        for (int i = 0; i < _firingEventTypes.length; i++) {
            // Configure the DebugProfile with the selected FiringEventTypes.
            if (getBooleanValue(_firingEventTypeLabels[i])) {
                _actorProfile.listenForEvent(_firingEventTypes[i]);
                breakpointsSelected = true;
            } else {
                _actorProfile.unlistenForEvent(_firingEventTypes[i]);
            }
        }

        // The director should not be null because we already checked
        // in the constructor.
        Director director = _actor.getExecutiveDirector();
        DebugController debugController = (DebugController) director
                .getAttribute(_DEBUGCONTROLLER);

        // If some breakpoints were selected
        if (breakpointsSelected) {
            if (debugController != null) {
                // Add this actor to the set of objects being debugged.
                debugController.putDebugProfile(_actor, _actorProfile);
            } else {
                // If the director does not already have a
                // DebugController, create one.
                String moml = "<property name=\""
                        + _DEBUGCONTROLLER
                        + "\" class=\"ptolemy.vergil.debugger.DebugController\"/>";
                ChangeRequest request = new MoMLChangeRequest(this, // originator
                        director, // context
                        moml);
                request.addChangeListener(this);
                director.requestChange(request);
            }
        } else {
            // If BreakpointConfigurerDialog()._handlClosing()
            // calls this in appropriately, then debugController might be
            // null.
            if (debugController != null) {
                // Remove profile if there are no longer any
                // breakpoints selected for this _actor.
                debugController.removeDebugProfile(_actor);
            }

            // FIXME: removeDebugListener if no more actors have breakpoints.
        }
    }

    /** React to a change request has been successfully executed.
     *  This method is called after a change request
     *  has been executed successfully.
     *  @param change The change that has been executed, or null if
     *   the change was not done via a ChangeRequest.
     */
    @Override
    public void changeExecuted(ChangeRequest change) {
        Director director = _actor.getExecutiveDirector();
        // The DebugController should not be null since the change
        // request should have added the DebugController to the
        // director.
        DebugController debugController = (DebugController) director
                .getAttribute(_DEBUGCONTROLLER);

        // Register a new DebugController with the director.
        director.addDebugListener(debugController);

        // Add this actor to the set of objects being debugged.
        debugController.putDebugProfile(_actor, _actorProfile);

        director.removeChangeListener(this);
    }

    /** React to a change request has resulted in an exception.
     *  This method is called after a change request was executed,
     *  but during the execution an exception was thrown.
     *  @param change The change that was attempted or null if
     *   the change was not done via a ChangeRequest.
     *  @param exception The exception that resulted.
     */
    @Override
    public void changeFailed(ChangeRequest change, Exception exception) {
        throw new InternalErrorException(
                "Could not add DebugController to the director");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Firing event type labels.
     * To add firing events for debugging, you must make changes in 2
     * places in this file: _firingEventTypeLabels, _firingEventTypes
     * Labels of FiringEventTypes to show in the dialog box.
     */
    protected static String[] _firingEventTypeLabels = {

        // FIXME: Only BEFORE_ITERATE and AFTER_ITERATE work with SDF
        //"before prefire",
        //"after prefire",
        //"before fire",
        //"after fire",
        //"before postfire",
        //"after postfire",
        "before iterate", "after iterate" };

    /** FiringEventTypes that the user can set breakpoints on. */
    protected static FiringEventType[] _firingEventTypes = {

        // FIXME: Only BEFORE_ITERATE and AFTER_ITERATE work with SDF
        //FiringEvent.BEFORE_PREFIRE,
        //FiringEvent.AFTER_PREFIRE,
        //FiringEvent.BEFORE_FIRE,
        //FiringEvent.AFTER_FIRE,
        //FiringEvent.BEFORE_POSTFIRE,
        //FiringEvent.AFTER_POSTFIRE,
        FiringEvent.BEFORE_ITERATE, FiringEvent.AFTER_ITERATE };

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Name of the DebugController to attach to the director.
    private static String _DEBUGCONTROLLER = "_DebugController";

    // The object that this configurer configures.
    private Actor _actor;

    // DebugProfile associated with _actor.
    private DebugProfile _actorProfile;

    // The GraphController associated with _actor.
    private BasicGraphController _graphController;

    // The class of ptolemy.domains.sdf.Director
    private static Class _sdfDirectorClass = null;
}
