/*

 Copyright (c) 2003-2007 The Regents of the University of California.
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

import java.util.Set;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gt.ingredients.criteria.Criterion;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.vergil.gt.GTIngredientsEditor;

//////////////////////////////////////////////////////////////////////////
//// CompositeActorMatcher

/**

@author Thomas Huining Feng
@version $Id$
@since Ptolemy II 6.1
@Pt.ProposedRating Red (tfeng)
@Pt.AcceptedRating Red (tfeng)
*/
public class CompositeActorMatcher extends TypedCompositeActor implements
        GTEntity, ValueListener {

    public CompositeActorMatcher(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        setClassName("ptolemy.actor.gt.CompositeActorMatcher");

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
        scopeExtender = new ActorScopeExtender(this, "scopeExtender");
        
        _attachText("_iconDescription", _ICON_DESCRIPTION);
    }

    public Criterion get(String name) {
        // TODO
        return null;
    }

    public GTIngredientsAttribute getCriteriaAttribute() {
        return criteria;
    }

    public String getDefaultIconDescription() {
        return _ICON_DESCRIPTION;
    }

    public GTIngredientsAttribute getOperationsAttribute() {
        return operations;
    }

    public PatternObjectAttribute getPatternObjectAttribute() {
        return patternObject;
    }

    public Set<String> labelSet() {
        // TODO
        return null;
    }

    public void updateAppearance(GTIngredientsAttribute attribute) {
        GTEntityUtils.updateAppearance(this, attribute);
    }

    public void valueChanged(Settable settable) {
        GTEntityUtils.valueChanged(this, settable);
    }

    public GTIngredientsAttribute criteria;

    public GTIngredientsEditor.Factory editorFactory;

    public GTIngredientsAttribute operations;

    public PatternObjectAttribute patternObject;

    public ActorScopeExtender scopeExtender;

    private static final String _ICON_DESCRIPTION = "<svg>"
        + "<rect x=\"0\" y=\"0\" width=\"60\" height=\"40\""
        + "  style=\"fill:#FF0000\"/>"
        + "<rect x=\"2\" y=\"2\" width=\"56\" height=\"36\""
        + "  style=\"fill:#C0C0C0\"/>"
        + "<rect x=\"6\" y=\"17\" width=\"16\" height=\"10\""
        + "  style=\"fill:#FFFFFF; stroke:#B00000\"/>"
        + "<rect x=\"38\" y=\"25\" width=\"16\" height=\"10\""
        + "  style=\"fill:#FFFFFF; stroke:#B00000\"/>"
        + "<line x1=\"26\" y1=\"22\" x2=\"30\" y2=\"22\""
        + "  style=\"stroke:#404040\"/>"
        + "<line x1=\"30\" y1=\"22\" x2=\"30\" y2=\"30\""
        + "  style=\"stroke:#404040\"/>"
        + "<line x1=\"30\" y1=\"30\" x2=\"34\" y2=\"30\""
        + "  style=\"stroke:#404040\"/>"
        + "<text x=\"16\" y=\"14\""
        + "  style=\"font-size:12; fill:#E00000; font-family:SansSerif\">"
        + "  match</text>" + "</svg>";

}
