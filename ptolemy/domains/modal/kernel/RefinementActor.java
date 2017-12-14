/* An interface for actors that act as refinements of states in FSMs or events
 in Ptera models.

 Copyright (c) 2008-2014 The Regents of the University of California.
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
package ptolemy.domains.modal.kernel;

import ptolemy.actor.InstanceOpener;
import ptolemy.actor.TypedActor;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// RefinementActor

/**
 An interface for actors that act as refinements of states in FSMs or events in
 Ptera models.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public interface RefinementActor extends TypedActor {

    /** Create a refinement for the given state.
     *  @param state The state that will contain the new refinement.
     *  @param name The name of the composite entity that stores the refinement.
     *  @param template The template used to create the refinement, or null if
     *   template is not used.
     *  @param className The class name for the refinement, which is used when
     *   template is null.
     *  @param instanceOpener The instanceOpener, typically a
     *   Configuration, that is used to open the refinement (as a
     *   look-inside action) after it is created, or null if it is not
     *   needed to open the refinement.
     *  @exception IllegalActionException If error occurs while creating the
     *   refinement.
     */
    public void addRefinement(State state, String name, Entity template,
            String className, InstanceOpener instanceOpener)
            throws IllegalActionException;

    /** Return the state (or event, which subclasses state) that this actor
     *  refines.
     *  @return The state or event that this actor refines.
     *  @exception IllegalActionException If thrown while trying to find the
     *  refined state or event.
     */
    public State getRefinedState() throws IllegalActionException;

    /** Control whether adding a port should be mirrored in the modal
     *  model and the mode controller.
     *  @param disable 0 if mirroring should occur, -1
     *   if mirroring should not occur downwards in the hierarchy,
     *   1 if mirroring should not occur upwards in the hierarchy.
     */
    public void setMirrorDisable(int disable);
}
