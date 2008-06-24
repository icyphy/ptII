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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Director;
import ptolemy.actor.Initializable;
import ptolemy.actor.Manager;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedActor;
import ptolemy.actor.gt.ingredients.criteria.AttributeCriterion;
import ptolemy.actor.gt.ingredients.criteria.Criterion;
import ptolemy.actor.util.FunctionDependency;
import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.vergil.gt.GTIngredientsEditor;
import ptolemy.vergil.gt.StateMatcherController;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class StateMatcher extends State implements GTEntity, TypedActor,
ValueListener {

    /**
     * @param container
     * @param name
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public StateMatcher(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        setClassName("ptolemy.actor.gt.StateMatcher");

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

    public void addInitializable(Initializable initializable) {
    }

    public void fire() throws IllegalActionException {
    }

    /** Return the attribute that stores all the criteria for this matcher.
     *
     *  @return The attribute that stores all the criteria.
     */
    public GTIngredientsAttribute getCriteriaAttribute() {
        return criteria;
    }

    public String getDefaultIconDescription() {
        return null;
    }

    public Director getDirector() {
        return null;
    }

    public Director getExecutiveDirector() {
        return null;
    }

    public FunctionDependency getFunctionDependency() {
        return null;
    }

    /** Return a token that contains an ingredient with the given name contained
     *  by this entity, or <tt>null</tt> if the ingredient cannot be resolved.
     *
     *  @param name The name of the ingredient.
     *  @return The token containing the ingredient object.
     */
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

    public Manager getManager() {
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

    public void initialize() throws IllegalActionException {
    }

    public List<?> inputPortList() {
        return _EMPTY_LIST;
    }

    public boolean isFireFunctional() {
        return false;
    }

    public boolean isStrict() {
        return false;
    }

    public int iterate(int count) throws IllegalActionException {
        return 0;
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
        return object instanceof State;
    }

    public Receiver newReceiver() throws IllegalActionException {
        return null;
    }

    public List<?> outputPortList() {
        return _EMPTY_LIST;
    }

    public boolean postfire() throws IllegalActionException {
        return false;
    }

    public boolean prefire() throws IllegalActionException {
        return false;
    }

    public void preinitialize() throws IllegalActionException {
    }

    public void removeInitializable(Initializable initializable) {
    }

    public void setContainer(CompositeEntity container)
    throws IllegalActionException, NameDuplicationException {
        super.setContainer(container);

        Attribute factory = getAttribute("_controllerFactory");
        if (container == null
                || !(container.getContainer() instanceof ModalModelMatcher)) {
            if (factory != null) {
                factory.setContainer(null);
            }
        } else {
            if (factory == null) {
                new StateMatcherController.Factory(this, "_controllerFactory");
            }
        }
    }

    public void stop() {
    }

    public void stopFire() {
    }

    public void terminate() {
    }

    public Set<Inequality> typeConstraints() throws IllegalActionException {
        return _EMPTY_SET;
    }

    /** Update appearance of this entity.
     *
     *  @param attribute The attribute containing ingredients of this entity.
     *  @see GTEntityUtils#updateAppearance(GTEntity, GTIngredientsAttribute)
     */
    public void updateAppearance(GTIngredientsAttribute attribute) {
        // GTEntityUtils.updateAppearance(this, attribute);
    }

    /** React to the fact that the specified Settable has changed.
     *
     *  @param settable The object that has changed value.
     *  @see GTEntityUtils#valueChanged(GTEntity, Settable)
     */
    public void valueChanged(Settable settable) {
        GTEntityUtils.valueChanged(this, settable);
    }

    public void wrapup() throws IllegalActionException {
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

    private static final List<?> _EMPTY_LIST = new LinkedList<Object>();
    private static final Set<Inequality> _EMPTY_SET = new HashSet<Inequality>();

    /** Cache of the label set.
     */
    private Set<String> _labelSet;

    /** The workspace version the last time when _labelSet was updated.
     */
    private long _version = -1;
}
