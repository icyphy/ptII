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
import ptolemy.actor.gt.rules.PortRule;
import ptolemy.actor.gt.rules.SubclassRule;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ConfigurableAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
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
public class AtomicActorMatcher extends TypedAtomicActor {

    public AtomicActorMatcher(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        ruleListAttribute = new RuleListAttribute(this, "ruleList");
        ruleListAttribute.setExpression("");
    }

    public void attributeChanged(Attribute attribute)
    throws IllegalActionException {
        super.attributeChanged(attribute);

        if (attribute == ruleListAttribute) {
            try {
                _workspace.getWriteAccess();

                Set<String> preservedPortNames = new HashSet<String>();
                boolean isIconSet = false;
                int i = 1;
                RuleList ruleList = ruleListAttribute.getRuleList();
                for (Rule rule : ruleList) {
                    if (rule instanceof PortRule) {
                        PortRule portRule = (PortRule) rule;
                        String portID = portRule.getPortID(ruleList);
                        preservedPortNames.add(portID);

                        TypedIOPort port = (TypedIOPort) getPort(portID);
                        if (port != null) {
                            port.setInput(portRule.isInput());
                            port.setOutput(portRule.isOutput());
                            port.setMultiport(portRule.isMultiport());
                            port.setPersistent(false);
                        } else {
                            port = new TypedIOPort(this, portID,
                                    portRule.isInput(), portRule.isOutput());
                            port.setMultiport(portRule.isMultiport());
                            port.setPersistent(false);
                        }
                        port.setPersistent(false);
                    } else if (rule instanceof SubclassRule && !isIconSet) {
                        SubclassRule subclassRule = (SubclassRule) rule;
                        String superclass = subclassRule.getSuperclass();
                        isIconSet = _loadActorIcon(superclass);
                    }
                    i++;
                }
                if (!isIconSet) {
                    _removeEditorIcons();
                    _setIconDescription(_ICON_DESCRIPTION);
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
            } catch (MalformedStringException e) {
                throw new IllegalActionException(null, e,
                        "ruleList attribute is malformed.");
            } catch (NameDuplicationException e) {
                throw new IllegalActionException(null, e,
                        "Name duplicated.");
            } finally {
                _workspace.doneWriting();
            }
        }
    }

    public RuleListAttribute ruleListAttribute;

    private boolean _loadActorIcon(String actorClassName) {
        try {
            Class<?> actorClass = Class.forName(actorClassName);
            CompositeActor container = new CompositeActor();
            String moml = "<group>"
                + "<entity name=\"NewActor\" class=\"" + actorClassName + "\"/>"
                + "</group>";
            container.requestChange(
                    new MoMLChangeRequest(this, container, moml));
            ComponentEntity actor =
                (ComponentEntity) container.entityList(actorClass).get(0);

            ConfigurableAttribute actorAttribute =
                (ConfigurableAttribute) actor.getAttribute("_iconDescription");
            String iconDescription = actorAttribute.getConfigureText();
            _setIconDescription(iconDescription);

            List<?> editorIconList = actor.attributeList(EditorIcon.class);
            if (editorIconList.isEmpty()) {
                _removeEditorIcons();
            } else {
                for (Object editorIconObject : editorIconList) {
                    if (!editorIconObject.getClass().getName().equals(
                            "ptolemy.vergil.icon.EditorIcon")) {
                        continue;
                    }
                    EditorIcon editorIcon = (EditorIcon) editorIconObject;
                    requestChange(new MoMLChangeRequest(this, this,
                            editorIcon.exportMoML()));
                }
            }
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void _removeEditorIcons() {
        for (Object editorIconObject : attributeList(EditorIcon.class)) {
            EditorIcon editorIcon = (EditorIcon) editorIconObject;
            String moml =
                "<deleteProperty name=\"" + editorIcon.getName() + "\"/>";
            requestChange(new MoMLChangeRequest(this, this, moml));
        }
    }

    private void _setIconDescription(String iconDescription) {
        String moml = "<property name=\"_iconDescription\""
            + "  class=\"ptolemy.kernel.util.SingletonConfigurableAttribute\">"
            + "  <configure>" + iconDescription + "</configure>"
            + "</property>";
        requestChange(new MoMLChangeRequest(this, this, moml));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    private static final String _ICON_DESCRIPTION =
        "<svg>"
        + "<rect x=\"0\" y=\"0\" width=\"60\" height=\"35\""
        + "  style=\"fill:#C0C0C0\"/>"
        + "<rect x=\"5\" y=\"12\" width=\"16\" height=\"10\""
        + "  style=\"fill:#FFFFFF; stroke:#B00000\"/>"
        + "<rect x=\"39\" y=\"20\" width=\"16\" height=\"10\""
        + "  style=\"fill:#FFFFFF; stroke:#B00000\"/>"
        + "<line x1=\"25\" y1=\"17\" x2=\"30\" y2=\"17\""
        + "  style=\"stroke:#404040\"/>"
        + "<line x1=\"30\" y1=\"17\" x2=\"30\" y2=\"25\""
        + "  style=\"stroke:#404040\"/>"
        + "<line x1=\"30\" y1=\"25\" x2=\"35\" y2=\"25\""
        + "  style=\"stroke:#404040\"/>"
        + "<text x=\"20\" y=\"11\""
        + "  style=\"font-size:10; fill:#E00000; font-family:SansSerif\">"
        + "  match</text>"
        + "</svg>";

    private static final long serialVersionUID = 8775143612221394893L;
}
