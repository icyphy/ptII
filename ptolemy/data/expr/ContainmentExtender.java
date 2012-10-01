/* An interface to specify the containment relationship as seen by the model
designer.

@Copyright (c) 2008-2012 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.data.expr;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

/**
 An interface to specify the containment relationship as seen by the model
 designer. This interface can be implemented by an attribute (such as {@link
 ptolemy.domains.modal.kernel.ContainmentExtender}. Such an attribute defines a
 special containment relationship that is slightly different from the
 containment relationship defined by {@link NamedObj#getContainer()}. The {@link
 #getExtendedContainer()} method returns the container of the object that
 contains the implementing attribute. The returned container is supposed to be
 the object that visually contains the object that owns the implementing
 attribute, as seen by the model designer. In particular, for a modal model
 (either FSM or Ptera), even though a refinement is visually contained by a state
 or an event, {@link NamedObj#getContainer()} of that refinement does not return
 the state or event because of a difference between the visual representation
 and internal data representation. In that case, {@link #getExtendedContainer()}
 of this interface returns the state or event.
 <p>
 When the expression evaluator tries to resolve a variable name starting from an
 object, it checks whether an attribute implementing this interface is owned by
 the object if the variable cannot be found in that object. If one such
 attribute is found, the evaluator considers the container returned by {@link
 #getExtendedContainer()}, instead of the {@link NamedObj#getContainer()} that
 it normally uses.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public interface ContainmentExtender {

    /** Get an object with the given name within the container.
     *
     *  @param name The name of the object.
     *  @return The object, or null if not found.
     *  @exception IllegalActionException If exception occurs when trying to get
     *   the contained object.
     */
    public NamedObj getContainedObject(String name)
            throws IllegalActionException;

    /** Get the extended container.
     *
     *  @return The container.
     *  @exception IllegalActionException If exception occurs when trying to get
     *   the container.
     */
    public NamedObj getExtendedContainer() throws IllegalActionException;
}
