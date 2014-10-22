/* A matcher to match any attribute.

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
package ptolemy.actor.gt;

import java.util.HashSet;
import java.util.Set;

import ptolemy.actor.gt.ingredients.criteria.AttributeCriterion;
import ptolemy.actor.gt.ingredients.criteria.Criterion;
import ptolemy.actor.gt.ingredients.operations.Operation;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.gt.GTIngredientsEditor;

//////////////////////////////////////////////////////////////////////////
//// AttributeMatcher

/**
 A matcher to match any attribute. In the pattern of a {@link
 TransformationRule}, this matcher can be customized by instances of {@link
 Criterion}. In the replacement of a {@link TransformationRule}, operations can
 be specified for this matcher with instances of {@link Operation}. The
 operations will be performed on the attribute that is matched by the
 corresponding matcher in the pattern, and is preserved after the
 transformation.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class AttributeMatcher extends Attribute implements GTEntity {

    /** Construct an attribute matcher to be either contained in the pattern
     *  of a {@link TransformationRule} or in the replacement.
     *
     *  @param container The proposed container of this matcher.
     *  @param name The name of this matcher.
     *  @exception IllegalActionException If this actor cannot be contained by
     *   the proposed container.
     *  @exception NameDuplicationException If the name coincides with an entity
     *   already in the container.
     */
    public AttributeMatcher(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        criteria = new GTIngredientsAttribute(this, "criteria");
        criteria.setExpression("");

        operations = new GTIngredientsAttribute(this, "operations");
        operations.setExpression("");

        patternObject = new PatternObjectAttribute(this, "patternObject");
        patternObject.setExpression("");

        editorFactory = new GTIngredientsEditor.Factory(this, "editorFactory");

        _attachText("_iconDescription", _ICON_DESCRIPTION);
    }

    /** Return the attribute that stores all the criteria for this matcher.
     *
     *  @return The attribute that stores all the criteria.
     */
    @Override
    public GTIngredientsAttribute getCriteriaAttribute() {
        return criteria;
    }

    /** Return a string that contains the SVG icon description
     *  ("&lt;svg&gt;...&lt;/svg&gt;") for this matcher. This icon description
     *  is the default icon for the matcher, which may be changed by the
     *  criteria.
     *
     *  @return The icon description.
     */
    @Override
    public String getDefaultIconDescription() {
        return _ICON_DESCRIPTION;
    }

    /** Return the attribute that stores all the operations for this matcher.
     *
     *  @return The attribute that stores all the operations.
     */
    @Override
    public GTIngredientsAttribute getOperationsAttribute() {
        return operations;
    }

    /** Return the attribute that stores the name of the corresponding entity in
     *  the pattern of the same {@link TransformationRule}, if this entity is in
     *  the replacement, or <tt>null</tt> otherwise.
     *
     *  @return The attribute that stores the name of the corresponding entity.
     *  @see #labelSet()
     */
    @Override
    public PatternObjectAttribute getPatternObjectAttribute() {
        return patternObject;
    }

    /** Return the set of names of ingredients contained in this entity that can
     *  be resolved.
     *
     *  @return The set of names.
     */
    @Override
    public Set<String> labelSet() {
        long version = workspace().getVersion();
        if (_labelSet == null || version > _version) {
            _labelSet = new HashSet<String>();
            try {
                int i = 0;
                for (GTIngredient ingredient : criteria.getIngredientList()) {
                    i++;
                    Criterion criterion = (Criterion) ingredient;
                    if (criterion instanceof AttributeCriterion) {
                        _labelSet.add("criterion" + i);
                    }
                }
            } catch (MalformedStringException e) {
                return _labelSet;
            }
        }
        return _labelSet;
    }

    /** Test whether this AtomicActorMatcher can match the given object. The
     *  matching is shallow in the sense that objects contained by this matcher
     *  need not match the corresponding objects in the given object for the
     *  return result to be true. The return result is true if and only if the
     *  given object is an instance of {@link ComponentEntity}.
     *
     *  @param object The NamedObj.
     *  @return Whether this AtomicActorMatcher can match the given object.
     */
    @Override
    public boolean match(NamedObj object) {
        return object instanceof Attribute;
    }

    /** Do nothing. The appearance of an AttributeMatcher is not updated with
     *  change of the criteria or operations.
     *
     *  @param attribute The attribute containing ingredients of this entity.
     */
    @Override
    public void updateAppearance(GTIngredientsAttribute attribute) {
    }

    /** The attribute containing all the criteria in a list
     *  ({@link GTIngredientList}).
     */
    public GTIngredientsAttribute criteria;

    /** The editor factory for ingredients in this matcher.
     */
    public GTIngredientsEditor.Factory editorFactory;

    /** The attribute containing all the operations in a list
     *  ({@link GTIngredientList}).
     */
    public GTIngredientsAttribute operations;

    /** The attribute that specifies the name of the corresponding entity in the
     *  pattern.
     */
    public PatternObjectAttribute patternObject;

    /** The default icon description.
     */
    private static final String _ICON_DESCRIPTION = "<svg>"
            + "<rect x=\"0\" y=\"0\" width=\"60\" height=\"10\""
            + "  style=\"fill:#C0C0C0\"/>" + "</svg>";

    /** Cache of the label set.
     */
    private Set<String> _labelSet;

    /** The workspace version the last time when _labelSet was updated.
     */
    private long _version = -1;
}
