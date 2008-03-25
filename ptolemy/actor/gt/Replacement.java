/*

@Copyright (c) 1997-2008 The Regents of the University of California.
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

import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Replacement extends CompositeActorMatcher {

    /**
     * @param container
     * @param name
     * @exception NameDuplicationException
     * @exception IllegalActionException
     */
    public Replacement(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        setClassName("ptolemy.actor.gt.Replacement");
    }

    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        super.setContainer(container);

        if (container instanceof TransformationRule) {
            if (patternParameter != null) {
                patternParameter.setContainer(null);
            }

            Pattern pattern = ((TransformationRule) container).getPattern();
            if (pattern != null) {
                patternParameter = new Parameter(this, "pattern");
                patternParameter.setToken(new NamedObjToken(pattern));
                patternParameter.setPersistent(false);
            }
        }
    }

    public Parameter patternParameter;

    protected void updateEntitiesAppearance(GTIngredientsAttribute attribute) {
        _updateEntitiesAppearance(this, attribute);
    }

    private static void _updateEntitiesAppearance(CompositeEntity container,
            GTIngredientsAttribute attribute) {
        for (Object entity : container.entityList()) {
            if (entity instanceof GTEntity) {
                GTEntity gtEntity = (GTEntity) entity;
                gtEntity.updateAppearance(attribute);
            }
            if (entity instanceof CompositeEntity) {
                _updateEntitiesAppearance((CompositeEntity) entity, attribute);
            }
        }
    }

}
