/*

@Copyright (c) 2008 The Regents of the University of California.
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
import ptolemy.actor.gt.ingredients.criteria.PortCriterion;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.StringUtilities;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class PortMatcher extends TypedIOPort {

    public PortMatcher(PortCriterion criterion, ComponentEntity container,
            String name, boolean isInput, boolean isOutput)
    throws IllegalActionException, NameDuplicationException {
        super(container, name, isInput, isOutput);
        _criterion = criterion;
    }

    public PortCriterion getPortCriterion() {
        return _criterion;
    }

    public void setContainer(Entity container) throws IllegalActionException,
    NameDuplicationException {
        if (container == null && _criterion != null) {
            GTIngredientList list = _criterion.getOwner();
            GTIngredientsAttribute attribute = list.getOwner();
            GTIngredientList newList = new GTIngredientList(attribute, list);
            newList.remove(_criterion);
            String moml = "<property name=\"" + attribute.getName()
                    + "\" value=\""
                    + StringUtilities.escapeForXML(newList.toString())
                    + "\"/>";
            MoMLChangeRequest request =
                new MoMLChangeRequest(this, getContainer(), moml);
            request.setUndoable(true);
            attribute.requestChange(request);
            _criterion = null;
        } else {
            super.setContainer(container);
        }
    }

    protected void _setPortCriterion(PortCriterion criterion) {
        _criterion = criterion;
    }

    private PortCriterion _criterion;
}
