/* A matcher to match a port.

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

package ptolemy.actor.gt;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gt.ingredients.criteria.Criterion;
import ptolemy.actor.gt.ingredients.criteria.PortCriterion;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.StringUtilities;

/**
 A matcher to match a port.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class PortMatcher extends TypedIOPort implements Checkable {

    /** Construct a port matcher with a container and a name that is
     *  either an input, an output, or both, depending on the third
     *  and fourth arguments. The specified container must implement
     *  the TypedActor interface or an exception will be thrown.
     *
     *  @param criterion The criterion for this port matcher, or null if this
     *   port matcher is not created for a PortCriterion.
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @param isInput True if this is to be an input port.
     *  @param isOutput True if this is to be an output port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   TypedActor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public PortMatcher(PortCriterion criterion, ComponentEntity container,
            String name, boolean isInput, boolean isOutput)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, isInput, isOutput);
        _criterion = criterion;
    }

    /** Return the criterion.
     *
     *  @return The criterion, or null if this port is not created for a
     *   PortCriterion.
     */
    @Override
    public Criterion getCriterion() {
        return _criterion;
    }

    /** Set container of this port matcher. If this port matcher is to be
     *  removed and it was created for a PortCriterion, remove the criterion
     *  from the container of this port.
     *
     *  @param container The new container, or null if this port matcher is to
     *   be removed.
     *  @exception IllegalActionException If the proposed container is not a
     *   ComponentEntity, doesn't implement Actor, or has no name,
     *   or the port and container are not in the same workspace. Or
     *   it's not null
     *  @exception NameDuplicationException If the container already has
     *   a port with the name of this port.
     */
    @Override
    public void setContainer(Entity container) throws IllegalActionException,
            NameDuplicationException {
        if (container == null && _criterion != null) {
            GTIngredientList list = _criterion.getOwner();
            GTIngredientsAttribute attribute = list.getOwner();
            GTIngredientList newList = new GTIngredientList(attribute, list);
            newList.remove(_criterion);
            String moml = "<property name=\"" + attribute.getName()
                    + "\" value=\""
                    + StringUtilities.escapeForXML(newList.toString()) + "\"/>";
            MoMLChangeRequest request = new MoMLChangeRequest(this,
                    getContainer(), moml);
            request.setUndoable(true);
            attribute.requestChange(request);
            _criterion = null;
        } else {
            super.setContainer(container);
        }
    }

    /** Set the PortCriterion.
     *
     *  @param criterion The criterion.
     */
    protected void _setPortCriterion(PortCriterion criterion) {
        _criterion = criterion;
    }

    /** The criterion, or null.
     */
    private PortCriterion _criterion;
}
