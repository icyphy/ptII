/* An execution listener that suspends execution based on breakpoints.

 Copyright (c) 1999-2002 The Regents of the University of California.
 All rights reserved.  Permission is hereby granted, without written
 agreement and without license or royalty fees, to use, copy, modify,
 and distribute this software and its documentation for any purpose,
 provided that the above copyright notice and the following two
 paragraphs appear in all copies of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN
 IF THE UNIVERSITY OF CALIFORNIA HAVE BEEN ADVISED OF THE POSSIBILITY
 OF SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIM ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
 UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (celaine@eecs.berkeley.edu)
@AcceptedRating Red (celaine@eecs.berkeley.edu)
*/

package ptolemy.vergil.debugger;

import diva.canvas.Figure;

import ptolemy.actor.Actor;
import ptolemy.actor.Manager;
import ptolemy.actor.Executable;
import ptolemy.actor.FiringEvent;
import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.DebugListener;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.TransientSingletonConfigurableAttribute;
import ptolemy.vergil.basic.AbstractBasicGraphModel;
import ptolemy.vergil.basic.BasicGraphController;
import ptolemy.vergil.kernel.DebugRenderer;

import java.util.Hashtable;

////////////////////////////////////////////////////////////////////////
//// DebugController
/**
An execution listener that suspends execution based on breakpoints.
This class should be associated with a director.  This class keeps a
DebugProfile for each actor that belongs to that director and are
being debugged.

@see DebugProfile

@author Elaine Cheong
@version $Id$
*/
public class DebugController extends TransientSingletonConfigurableAttribute
    implements DebugListener {

    /** Construct a debug listener with the given container and name.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container does not accept
     *   this entity (this should not occur).
     *  @exception NameDuplicationException If the name coincides with an
     *   attribute already in the container.
     */
    public DebugController(NamedObj container, String name)
             throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _toDebug = new Hashtable();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clear the set of actors that are being debugged.
     */
    public void clear() {
        _toDebug.clear();
    }

    /** Respond to debug events of type FiringEvent by highlighting
     *  the actor that we are breaking on.
     *  @param debugEvent The debug event.
     *  @see ptolemy.vergil.actor.ActorViewerGraphController#event
     */
    public void event(DebugEvent debugEvent) {
        // FIXME: this method is called every time the director gets a
        // firing event for any actor...is this ok?

        // Ignore debug events that aren't firing events.
        if (debugEvent instanceof FiringEvent) {
            FiringEvent event = (FiringEvent) debugEvent;

            NamedObj objToHighlight = (NamedObj)event.getActor();

            if (_toDebug.containsKey(objToHighlight)) {
                // The actor associated with this firing event is in
                // the set of actors to be debugged.

                // If the object is not contained by the associated
                // composite, then find an object above it in the hierarchy
                // that is.
                DebugProfile debugProfile =
                    getDebug((Executable)objToHighlight);
                BasicGraphController graphController =
                    debugProfile.getGraphController();
                AbstractBasicGraphModel graphModel =
                    (AbstractBasicGraphModel)graphController.getGraphModel();
                NamedObj toplevel = graphModel.getPtolemyModel();

                while (objToHighlight != null
                        && objToHighlight.getContainer() != toplevel) {
                    objToHighlight = (NamedObj)objToHighlight.getContainer();
                }
                if (objToHighlight == null) {
                    return;
                }
                Object location = objToHighlight.getAttribute("_location");
                if (location != null) {
                    Figure figure = graphController.getFigure(location);
                    if (figure != null) {
                        // If the user has chosen to break on one of
                        // the firing events, highlight the actor and
                        // wait for keyboard input.
                        if (debugProfile.isListening(event.getType())) {
                            String message =
                                new String(objToHighlight.getName()
                                        + " "
                                        + event.getType().getName());
                            Manager manager =
                                ((Actor)objToHighlight).getManager();
                            render(figure, manager, message);
                        }
                    }
                }
            }
        }
    }

    /** Determine whether debugging is enabled on the set of actors.
     *  @return True if debugging is enabled.
     */
    public boolean getEnabled() {
        // FIXME: not implemented yet
        return false;
    }

    /** Get the profile for an actor that is being debugged.
     *  @param actor The actor for which to retrieve the profile.
     *  @return The profile for the actor.
     */
    public DebugProfile getDebug(Executable actor) {
        return (DebugProfile)_toDebug.get(actor);
    }

    /** Ignore string messages.
     */
    public void message(String string) {
    }

    /** Add an actor to the set of actors that are being debugged.
     *  @param actor The actor to debug.
     *  @param profile The breakpoint configuration for this actor.
     */
    public void setDebug(Executable actor, DebugProfile profile) {
        _toDebug.put(actor, profile);
    }

    /** Enable/disable debugging on the set of actors.
     *  @param enabled True if debugging should be enabled.
     */
    public void setEnabled(boolean enabled) {
        // FIXME: not implemented yet
    }

    /** Remove an actor from the set of actors that are being debugged.
     *  @param actor The actor to remove.
     */
    public void unsetDebug(Executable actor) {
        // delete the debug profile from the hashtable
        _toDebug.remove(actor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Highlight the actor.
     *  @param figure The figure that we are highlighting.
     *  @param manager The manager for the figure.
     *  @see ptolemy.vergil.kernel.DebugRenderer
     */
    private void render(Figure figure, Manager manager, String message) {
        if (_debugRenderer == null) {
            _debugRenderer = new DebugRenderer();
        }

        _debugRenderer.renderSelected(figure);
        Figure debugRendered = figure;

        // Wait for user to select Resume.
        manager.waitForResume(message);
        if (debugRendered != null) {
            // Unhighlight the actor after resuming execution.
            _debugRenderer.renderDeselected(debugRendered);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The _debugRenderer for _object.
    private DebugRenderer _debugRenderer = null;

    // The set of actors that are being debugged.
    private Hashtable _toDebug = null;
}
