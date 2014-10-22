/* An implementation of containment extender for modal models.

@Copyright (c) 2008-2014 The Regents of the University of California.
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

package ptolemy.domains.modal.kernel;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**
 An implementation of containment extender for modal models as an attribute.
 This attribute defines a special containment relationship that is slightly
 different from the containment relationship defined by {@link
 NamedObj#getContainer()}. The {@link #getExtendedContainer()} method returns
 the container of the object that contains the implementing attribute. The
 returned container is supposed to be the object that visually contains the
 object that owns the implementing attribute, as seen by the model designer. In
 particular, for a modal model (either FSM or Ptera), even though a refinement is
 visually contained by a state or an event, {@link NamedObj#getContainer()} of
 that refinement does not return the state or event because of a difference
 between the visual representation and internal data representation. In that
 case, {@link #getExtendedContainer()} of this class returns the state or event.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ContainmentExtender extends Attribute implements
        ptolemy.data.expr.ContainmentExtender {

    /** Construct a ContainmentExtender attribute with the given name contained
     *  by the specified Refinement. The container argument must not be null,
     *  or a NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ContainmentExtender(RefinementActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super((NamedObj) container, name);
        setPersistent(false);
    }

    /** Construct a ContainmentExtender attribute with the given name contained
     *  by the specified State. The container argument must not be null,
     *  or a NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ContainmentExtender(State container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setPersistent(false);
    }

    /** Get an object with the given name within the container.
     *
     *  @param name The name of the object.
     *  @return The object, or null if not found.
     *  @exception IllegalActionException If the refinement of the containing
     *   state cannot be found, or if a comma-separated list is malformed.
     */
    @Override
    public NamedObj getContainedObject(String name)
            throws IllegalActionException {
        NamedObj container = getContainer();
        if (container instanceof State) {
            return ((State) container).getObjectInRefinement(name);
        } else {
            return null;
        }
    }

    /** Get the extended container.
     *
     *  @return The container.
     *  @exception IllegalActionException If the specified refinement cannot be
     *   found in a state, or if a comma-separated list is malformed.
     */
    @Override
    public NamedObj getExtendedContainer() throws IllegalActionException {
        NamedObj container = getContainer();
        if (container instanceof RefinementActor) {
            return ((RefinementActor) container).getRefinedState();
        } else {
            return container.getContainer();
        }
    }
}
