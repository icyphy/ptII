/* An entity to be used to match a modal model or Ptera modal model.

 Copyright (c) 1997-2009 The Regents of the University of California.
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

package ptolemy.actor.gt;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.vergil.gt.GTTableau;

/**
 An entity to be used to match a modal model or Ptera modal model.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ModalModelMatcher extends CompositeActorMatcher {

    /** Construct a modal model matcher to be either contained in the pattern
     *  of a {@link TransformationRule} or in the replacement.
     *
     *  @param container The proposed container of this matcher.
     *  @param name The name of this matcher.
     *  @exception IllegalActionException If this actor cannot be contained by
     *   the proposed container.
     *  @exception NameDuplicationException If the name coincides with an entity
     *   already in the container.
     */
    public ModalModelMatcher(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        new FSMMatcher(this, "_Controller");
        new GTTableau.ModalTableauFactory(this, "_tableauFactory");
    }

    /** Get the FSM controller within this modal model matcher.
     *
     *  @return The FSM controller (which is a matcher to match an FSM).
     */
    public FSMMatcher getController() {
        return (FSMMatcher) getEntity("_Controller");
    }
}
