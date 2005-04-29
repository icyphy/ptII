/* Profile for an actor being debugged.

Copyright (c) 1998-2005 The Regents of the University of California.
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

import java.util.HashSet;

import ptolemy.actor.FiringEvent.FiringEventType;
import ptolemy.vergil.basic.BasicGraphController;


//////////////////////////////////////////////////////////////////////////
//// DebugProfile

/**
   Profile for an actor being debugged.  Contains the FiringEventTypes on
   which the actor should break.

   @author Elaine Cheong
   @version $Id$
   @since Ptolemy II 2.1
   @Pt.ProposedRating Red (celaine)
   @Pt.AcceptedRating Red (celaine)
*/
public class DebugProfile {
    /** Construct a debug profile for an actor with the associated
     *  GraphController.
     *  @param graphController The GraphController.
     */
    public DebugProfile(BasicGraphController graphController) {
        _graphController = graphController;
        _firingEventTypes = new HashSet();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the GraphController.
     *  @return The GraphController.
     */
    public BasicGraphController getGraphController() {
        return _graphController;
    }

    /** See if the DebugProfile contains this FiringEventType.
     *  @param type the FiringEventType.
     *  @return True if the DebugProfile contains this FiringEventType.
     */
    public boolean isListening(FiringEventType type) {
        return _firingEventTypes.contains(type);
    }

    /** Add this FiringEventType to the DebugProfile.
     *  @param type the FiringEventType.
     */
    public void listenForEvent(FiringEventType type) {
        _firingEventTypes.add(type);
    }

    /** See if the DebugProfile contains this FiringEventType.
     *  @param type the FiringEventType.
     *  @return True if the DebugProfile contains this FiringEventType.
     */
    public boolean matches(FiringEventType type) {
        // FIXME: is this method needed?
        return false;
    }

    /** Remove this FiringEventType from the DebugProfile.
     *  @param type the FiringEventType.
     */
    public void unlistenForEvent(FiringEventType type) {
        _firingEventTypes.remove(type);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Set of firing event types we want to break on.
    private HashSet _firingEventTypes;

    // The GraphController associated with this actor.
    private BasicGraphController _graphController;
}
