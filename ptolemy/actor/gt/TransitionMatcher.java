/*

 Copyright (c) 1997-2005 The Regents of the University of California.
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
import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.vergil.gt.GTIngredientsEditor;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class TransitionMatcher extends Transition implements GTEntity,
ValueListener {

    /**
     * @param container
     * @param name
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public TransitionMatcher(FSMActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        setClassName("ptolemy.actor.gt.TransitionMatcher");

        criteria = new GTIngredientsAttribute(this, "criteria");
        criteria.setExpression("");
        criteria.addValueListener(this);

        operations = new GTIngredientsAttribute(this, "operations");
        operations.setExpression("");
        operations.addValueListener(this);

        patternObject = new PatternObjectAttribute(this, "patternObject");
        patternObject.setExpression("");
        patternObject.addValueListener(this);

        editorFactory = new GTIngredientsEditor.Factory(this, "editorFactory");
    }

    public GTIngredientsAttribute getCriteriaAttribute() {
        return criteria;
    }

    public String getDefaultIconDescription() {
        return null;
    }

    public Token getIngredientToken(String name) {
        if (name.startsWith("criterion")) {
            String indexString = name.substring(9);
            try {
                int index = Integer.parseInt(indexString);
                GTIngredientList list = criteria.getIngredientList();
                GTIngredient ingredient = list.get(index - 1);
                if (ingredient instanceof AttributeCriterion) {
                    return new ObjectToken(ingredient, ingredient.getClass());
                }
            } catch (MalformedStringException e) {
            } catch (IllegalActionException e) {
            } catch (NumberFormatException e) {
            }
        }
        return null;
    }

    /** Return the attribute that stores all the operations for this matcher.
     *
     *  @return The attribute that stores all the operations.
     */
    public GTIngredientsAttribute getOperationsAttribute() {
        return operations;
    }

    /** Return the attribute that stores the name of the corresponding entity in
     *  the pattern of the same {@link TransformationRule}, if this entity is in
     *  the replacement, or <tt>null</tt> otherwise.
     *
     *  @return The attribute that stires the name of the corresponding entity.
     *  @see #labelSet()
     */
    public PatternObjectAttribute getPatternObjectAttribute() {
        return patternObject;
    }

    /** Return the set of names of ingredients contained in this entity that can
     *  be resolved.
     *
     *  @return The set of names.
     *  @see #getIngredientToken(String)
     */
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

    public boolean match(NamedObj object) {
        return object instanceof Transition;
    }

    public void updateAppearance(GTIngredientsAttribute attribute) {
        // GTEntityUtils.updateAppearance(this, attribute);
    }

    public void valueChanged(Settable settable) {
        GTEntityUtils.valueChanged(this, settable);
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

    /** Cache of the label set.
     */
    private Set<String> _labelSet;

    /** The workspace version the last time when _labelSet was updated.
     */
    private long _version = -1;
}
