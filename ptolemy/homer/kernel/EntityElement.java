/* The definition of entities.

Copyright (c) 2011-2014 The Regents of the University of California.
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

package ptolemy.homer.kernel;

import ptolemy.actor.injection.PortableContainer;
import ptolemy.actor.injection.PortablePlaceable;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// EntityElement

/** The definition of entities.
 *
 *  @author Peter Foldes
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 */
public class EntityElement extends PositionableElement {

    ///////////////////////////////////////////////////////////////////
    ////                constructor                                ////

    /** Parse the Ptolemy attribute element.
     *  @param entity The Ptolemy StringAttribute with all the
     *  information.
     *  @exception IllegalActionException If entity does not implement
     *  PortablePlaceable.
     */
    public EntityElement(ComponentEntity entity) throws IllegalActionException {
        super(entity);

        // Check if the different representation injection is available
        // for the entity.
        if (!(entity instanceof PortablePlaceable)) {
            throw new IllegalActionException("Entity " + entity.getFullName()
                    + " is not portable.");
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a representation and add it to the passed container.
     *
     *  @param container The container to place the representation into.
     */
    @Override
    public void addToContainer(PortableContainer container) {
        ((PortablePlaceable) getElement()).place(container);
    }
}
