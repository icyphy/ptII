/* A choice element for a criterion.

 Copyright (c) 1997-2009 The Regents of the University of California.
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

package ptolemy.actor.gt.ingredients.criteria;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// ChoiceCriterionElement

/**
 A choice element for a criterion.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ChoiceCriterionElement extends StringCriterionElement {

    /** Construct a choice element for a criterion.
     *
     *  @param name The name of the element.
     *  @param canDisable Whether the element can be disabled.
     */
    public ChoiceCriterionElement(String name, boolean canDisable) {
        this(name, canDisable, false, false, false);
    }

    /** Construct a choice element for a criterion.
     *
     *  @param name The name of the element.
     *  @param canDisable Whether the element can be disabled.
     *  @param acceptRegularExpression Whether regular expression is accepted.
     *  @param acceptPtolemyExpression Whether Ptolemy expression is accepted.
     *  @param editable Whether a new value can be input in the edit box.
     */
    public ChoiceCriterionElement(String name, boolean canDisable,
            boolean acceptRegularExpression, boolean acceptPtolemyExpression,
            boolean editable) {
        super(name, canDisable, acceptRegularExpression,
                acceptPtolemyExpression);
        _editable = editable;
    }

    /** Add a choice to the end of choices.
     *
     *  @param choice The new choice.
     */
    public void addChoice(Object choice) {
        _choices.add(choice);
    }

    /** Add choices to the end of choices.
     *
     *  @param choices The new choices.
     */
    public void addChoices(Collection<?> choices) {
        _choices.addAll(choices);
    }

    /** Get an unmodifiable list of all the choices.
     *
     *  @return The list.
     */
    public List<Object> getChoices() {
        return Collections.unmodifiableList(_choices);
    }

    /** Return whether a new value can be input in the edit box.
     *
     *  @return true if a new value can be input in the edit box.
     */
    public boolean isEditable() {
        return _editable;
    }

    /** Remove a choice from the list of choices.
     *
     *  @param choice The choice to be removed.
     */
    public void removeChoice(Object choice) {
        _choices.remove(choice);
    }

    /** Remove choices from the list of choices.
     *
     *  @param choices The choices to be removed.
     */
    public void removeChoices(Collection<?> choices) {
        _choices.removeAll(choices);
    }

    /** The list of choices.
     */
    private List<Object> _choices = new LinkedList<Object>();

    /** Whether a new value can be input in the edit box.
     */
    private boolean _editable;
}
