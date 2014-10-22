/* The replacement of a transformation rule.

@Copyright (c) 2007-2014 The Regents of the University of California.
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

import ptolemy.data.ObjectToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**
 The replacement of a transformation rule.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Replacement extends CompositeActorMatcher {

    /** Construct an atomic actor matcher to be either contained in the pattern
     *  of a {@link TransformationRule} or in the replacement.
     *
     *  @param container The proposed container of this matcher.
     *  @param name The name of this matcher.
     *  @exception IllegalActionException If this actor cannot be contained by
     *   the proposed container.
     *  @exception NameDuplicationException If the name coincides with an entity
     *   already in the container.
     */
    public Replacement(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        setClassName("ptolemy.actor.gt.Replacement");
    }

    /** Set the container of this replacement, which should be a {@link
     *  TransformationRule}. Update patternParameter to contain an {@link
     *  ObjectToken} encapsulating the pattern with name "pattern" (so that the
     *  name "pattern" can be used in any expression in this replacement).
     *
     *  @param container The new container, or null.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the name of this entity.
     */
    @Override
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
                patternParameter.setToken(new ObjectToken(pattern));
                patternParameter.setPersistent(false);
            }
        }
    }

    /** The parameter containing an {@link ObjectToken} encapsulating the
     *  pattern with name "pattern".
     */
    public Parameter patternParameter;

    /** Update the appearance of the entities within this replacement for the
     *  change of the given attribute, so that the entities with correspondence
     *  in the pattern appears to be the same as the changed objects in the
     *  pattern.
     *
     *  @param attribute The attribute of any object in the pattern that was
     *   changed.
     */
    protected void updateEntitiesAppearance(GTIngredientsAttribute attribute) {
        _updateEntitiesAppearance(this, attribute);
    }

    /** Update the appearance of the entities within the given container for the
     *  change of the given attribute, so that the entities with correspondence
     *  in the pattern appears to be the same as the changed objects in the
     *  pattern.
     *
     *  @param container The container to be updated.
     *  @param attribute The attribute of any object in the pattern that was
     *   changed.
     */
    private static void _updateEntitiesAppearance(CompositeEntity container,
            GTIngredientsAttribute attribute) {
        try {
            container.workspace().getReadAccess();
            if (container instanceof GTEntity) {
                if (GTTools.getCorrespondingPatternObject(container) == attribute
                        .getContainer()) {
                    GTEntity gtEntity = (GTEntity) container;
                    gtEntity.updateAppearance(attribute);
                }
            }
            for (Object entity : container.entityList()) {
                if (entity instanceof GTEntity) {
                    if (GTTools
                            .getCorrespondingPatternObject((NamedObj) entity) == attribute
                            .getContainer()) {
                        GTEntity gtEntity = (GTEntity) entity;
                        gtEntity.updateAppearance(attribute);
                    }
                }
                if (entity instanceof CompositeEntity) {
                    _updateEntitiesAppearance((CompositeEntity) entity,
                            attribute);
                }
            }
        } finally {
            container.workspace().doneReading();
        }
    }

}
