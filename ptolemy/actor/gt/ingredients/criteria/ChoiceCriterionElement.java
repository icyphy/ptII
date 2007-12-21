/*

 Copyright (c) 1997-2007 The Regents of the University of California.
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

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ChoiceCriterionElement extends StringCriterionElement {

    public ChoiceCriterionElement(String name, boolean canDisable) {
        this(name, canDisable, false, false, false);
    }

    public ChoiceCriterionElement(String name, boolean canDisable,
            boolean acceptRegularExpression, boolean acceptPtolemyExpression,
            boolean editable) {
        super(name, canDisable, acceptRegularExpression,
                acceptPtolemyExpression);
        _editable = editable;
    }

    public void addChoice(Object choice) {
        _choices.add(choice);
    }

    public void addChoices(Collection<?> choices) {
        _choices.addAll(choices);
    }

    public List<Object> getChoices() {
        return Collections.unmodifiableList(_choices);
    }

    public boolean isEditable() {
        return _editable;
    }

    public void removeChoice(Object choice) {
        _choices.remove(choice);
    }

    public void removeChoices(Collection<?> choices) {
        _choices.removeAll(choices);
    }

    private List<Object> _choices = new LinkedList<Object>();

    private boolean _editable;
}
