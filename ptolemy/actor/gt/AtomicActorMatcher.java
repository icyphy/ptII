/*

 Copyright (c) 2003-2006 The Regents of the University of California.
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
import java.util.List;
import java.util.Set;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gt.ingredients.criteria.PortCriterion;
import ptolemy.actor.gt.ingredients.criteria.SubclassCriterion;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.ConfigurableAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.icon.EditorIcon;

//////////////////////////////////////////////////////////////////////////
//// AtomicActorMatcher

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class AtomicActorMatcher extends TypedAtomicActor implements GTEntity,
ValueListener {

    public AtomicActorMatcher(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        setClassName("ptolemy.actor.gt.AtomicActorMatcher");

        criteria = new GTIngredientsAttribute(this, "criteria");
        criteria.setExpression("");
        criteria.addValueListener(this);

        patternEntity = new PatternEntityAttribute(this, "patternEntity");
        patternEntity.setExpression("");
        patternEntity.addValueListener(this);

        _attachText("_iconDescription", _ICON_DESCRIPTION);
    }

    public PatternEntityAttribute getPatternEntityAttribute() {
        return patternEntity;
    }

    public void valueChanged(Settable settable) {
        try {
            if (settable == criteria) {
                if (GTEntityTools.isInPattern(this)) {
                    // ruleList attribute is used to set the matching rules for
                    // this actor. It is used only for actors in the pattern of
                    // a transformation rule. If the actor is in the
                    // replacement, this attribute is ignored.
                    _updateRuleListAttribute(criteria);
                }
            } else if (settable == patternEntity) {
                if (GTEntityTools.isInReplacement(this)) {
                    // Update the ports with the ruleList attribute of the
                    // corresponding actor in the pattern of the transformation
                    // rule.
                    GTEntity entity =
                        GTEntityTools.getCorrespondingPatternEntity(this);
                    if (entity != null
                            && entity instanceof AtomicActorMatcher) {
                        criteria.setPersistent(false);
                        criteria.setExpression("");
                        _updateRuleListAttribute(
                                ((AtomicActorMatcher) entity).criteria);
                    }
                }
            }
        } catch (KernelException e) {
            throw new KernelRuntimeException(e, "Unable to update the "
                    + settable.getName() + " attribute.");
        }
    }

    public GTIngredientsAttribute criteria;

    public PatternEntityAttribute patternEntity;

    private void _loadActorIcon(String actorClassName) {
        CompositeActor container = new CompositeActor();
        String moml = "<group><entity name=\"NewActor\" class=\""
            + actorClassName + "\"/></group>";
        container.requestChange(
                new LoadActorIconChangeRequest(container, moml));
    }

    private void _removeEditorIcons() {
        for (Object editorIconObject : attributeList(EditorIcon.class)) {
            EditorIcon editorIcon = (EditorIcon) editorIconObject;
            String moml =
                "<deleteProperty name=\"" + editorIcon.getName() + "\"/>";
            MoMLChangeRequest request = new MoMLChangeRequest(this, this, moml);
            request.execute();
        }
    }

    private void _setIconDescription(String iconDescription) {
        String moml = "<property name=\"_iconDescription\" class="
            + "\"ptolemy.kernel.util.SingletonConfigurableAttribute\">"
            + "  <configure>" + iconDescription + "</configure>"
            + "</property>";
        MoMLChangeRequest request = new MoMLChangeRequest(this, this, moml);
        request.execute();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    private void _updateRuleListAttribute(GTIngredientsAttribute ruleList)
    throws IllegalActionException, MalformedStringException,
    NameDuplicationException {

        try {
            _workspace.getWriteAccess();

            Set<String> preservedPortNames = new HashSet<String>();
            boolean isIconSet = false;
            int i = 1;
            GTIngredientList list = ruleList.getRuleList();
            for (GTIngredient rule : list) {
                if (rule instanceof PortCriterion) {
                    PortCriterion portRule = (PortCriterion) rule;
                    String portID = portRule.getPortID(list);
                    preservedPortNames.add(portID);

                    TypedIOPort port = (TypedIOPort) getPort(portID);
                    if (port != null) {
                        port.setInput(portRule.isInput());
                        port.setOutput(portRule.isOutput());
                        port.setMultiport(portRule.isMultiport());
                        port.setPersistent(false);
                    } else {
                        port = new TypedIOPort(this, portID, portRule.isInput(),
                                portRule.isOutput());
                        port.setMultiport(portRule.isMultiport());
                        port.setPersistent(false);
                    }
                    port.setPersistent(false);
                } else if (rule instanceof SubclassCriterion && !isIconSet) {
                    SubclassCriterion subclassRule = (SubclassCriterion) rule;
                    final String superclass = subclassRule.getSuperclass();
                    requestChange(new ChangeRequest(this,
                            "Deferred load actor icon action.") {
                        protected void _execute() {
                            _loadActorIcon(superclass);
                        }
                    });
                    isIconSet = true;
                }
                i++;
            }
            if (!isIconSet) {
                requestChange(new RestoreAppearanceChangeRequest());
            }

            List<?> portList = portList();
            for (i = 0; i < portList.size();) {
                Port port = (Port) portList.get(i);
                if (!preservedPortNames.contains(port.getName())) {
                    port.setContainer(null);
                } else {
                    i++;
                }
            }
            for (Object portObject : portList()) {
                Port port = (Port) portObject;
                if (!preservedPortNames.contains(port.getName())) {
                    port.setContainer(null);
                }
            }

        } finally {
            _workspace.doneWriting();
        }
    }

    private static final String _ICON_DESCRIPTION =
        "<svg>"
        + "<rect x=\"0\" y=\"0\" width=\"60\" height=\"40\""
        + "  style=\"fill:#C0C0C0\"/>"
        + "<rect x=\"5\" y=\"17\" width=\"16\" height=\"10\""
        + "  style=\"fill:#FFFFFF; stroke:#B00000\"/>"
        + "<rect x=\"39\" y=\"25\" width=\"16\" height=\"10\""
        + "  style=\"fill:#FFFFFF; stroke:#B00000\"/>"
        + "<line x1=\"25\" y1=\"22\" x2=\"30\" y2=\"22\""
        + "  style=\"stroke:#404040\"/>"
        + "<line x1=\"30\" y1=\"22\" x2=\"30\" y2=\"30\""
        + "  style=\"stroke:#404040\"/>"
        + "<line x1=\"30\" y1=\"30\" x2=\"35\" y2=\"30\""
        + "  style=\"stroke:#404040\"/>"
        + "<text x=\"17\" y=\"13\""
        + "  style=\"font-size:12; fill:#E00000; font-family:SansSerif\">"
        + "  match</text>"
        + "</svg>";

    private static final long serialVersionUID = 8775143612221394893L;

    private class LoadActorIconChangeRequest extends MoMLChangeRequest {

        public LoadActorIconChangeRequest(CompositeEntity container,
                String request) {
            super(AtomicActorMatcher.this, container, request);

            _container = container;
        }

        protected void _execute() {
            try {
                super._execute();

                ComponentEntity actor =
                    (ComponentEntity) _container.entityList().get(0);

                _removeEditorIcons();

                ConfigurableAttribute actorAttribute = (ConfigurableAttribute)
                        actor.getAttribute("_iconDescription");
                String iconDescription = actorAttribute.getConfigureText();
                _setIconDescription(iconDescription);

                List<?> editorIconList = actor.attributeList(EditorIcon.class);

                for (Object editorIconObject : editorIconList) {
                    EditorIcon editorIcon = (EditorIcon) editorIconObject;
                    editorIcon.setName("_icon");
                    String iconMoML = editorIcon.exportMoML();
                    new MoMLChangeRequest(this, AtomicActorMatcher.this,
                            iconMoML).execute();
                    break;
                }
            } catch (Exception e) {
                // Catches exceptions like KernelException,
                //     NullPointerException, IndexOutOfBoundsException, etc.
                // Rerestore the actor's original appearance.
                _removeEditorIcons();
                _setIconDescription(_ICON_DESCRIPTION);
            }
        }

        private CompositeEntity _container;
    }

    private class RestoreAppearanceChangeRequest extends ChangeRequest {

        protected void _execute() throws Exception {
            _removeEditorIcons();
            _setIconDescription(_ICON_DESCRIPTION);
        }

        RestoreAppearanceChangeRequest() {
            super(AtomicActorMatcher.this, "Restore the default appearance.");
        }
    }
}
