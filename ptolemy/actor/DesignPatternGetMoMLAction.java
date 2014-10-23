/* An action that generates the Moml for a group of states to be used as a
 design pattern.

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
package ptolemy.actor;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

/**
  An action that generates the Moml for a group of states to be used as a
  design pattern.

  @author Thomas Huining Feng
  @version $Id$
  @since Ptolemy II 8.0
  @Pt.ProposedRating Red (tfeng)
  @Pt.AcceptedRating Red (tfeng)
 */
public class DesignPatternGetMoMLAction {

    /** Generate the Moml string for the given object. If the object is a group
     *  of states, then the contents of the group are generated; otherwise, the
     *  Moml of the object itself is generated with {@link
     *  NamedObj#exportMoML(String)}.
     *
     *  @param object The object.
     *  @param name The name to be used for the object in the generated Moml.
     *  @return The Moml string.
     */
    public String getMoml(NamedObj object, String name) {
        CompositeEntity group = (CompositeEntity) object;
        Attribute before = object.getAttribute("Before");
        Attribute after = object.getAttribute("After");
        StringWriter buffer = new StringWriter();
        int extraIndent = 0;
        try {
            buffer.write("<group>\n");

            if (before != null) {
                String oldType = null;
                StringParameter typeParameter = (StringParameter) before
                        .getAttribute("_type");
                if (typeParameter == null) {
                    typeParameter = new StringParameter(before, "_type");
                } else {
                    oldType = typeParameter.getExpression();
                }
                typeParameter.setExpression("immediate");
                try {
                    buffer.write(StringUtilities.getIndentPrefix(1)
                            + "<group>\n");
                    before.exportMoML(buffer, 2);
                    buffer.write(StringUtilities.getIndentPrefix(1)
                            + "</group>\n");
                } finally {
                    if (oldType == null) {
                        typeParameter.setContainer(null);
                    } else {
                        typeParameter.setExpression(oldType);
                    }
                }
            }

            if (after != null || before != null) {
                extraIndent++;
                buffer.write(StringUtilities.getIndentPrefix(extraIndent)
                        + "<group>\n");
            }

            List<Attribute> attributes = group.attributeList();
            for (Attribute attribute : attributes) {
                if (!_IGNORED_ATTRIBUTES.contains(attribute.getName())
                        && (after == null || attribute != after)
                        && (before == null || attribute != before)) {
                    attribute.exportMoML(buffer, extraIndent + 1);
                    if (attribute instanceof Parameter) {
                        String parameterName = attribute.getName();
                        String expression = _overriddenParameters
                                .get(parameterName);
                        if (expression != null) {
                            buffer.write(StringUtilities
                                    .getIndentPrefix(extraIndent + 1)
                                    + "<property name=\""
                                    + parameterName
                                    + "\" value=\"" + expression + "\">\n");
                            buffer.write(StringUtilities
                                    .getIndentPrefix(extraIndent + 1)
                                    + "</property>\n");
                        }
                    }
                }
            }

            List<Port> ports = group.portList();
            for (Port port : ports) {
                buffer.write(StringUtilities.getIndentPrefix(extraIndent + 1)
                        + "<port name=\"" + port.getName() + "\">\n");
                if (port instanceof IOPort) {
                    IOPort ioPort = (IOPort) port;
                    boolean isInput = ioPort.isInput();
                    boolean isOutput = ioPort.isOutput();
                    if (isInput) {
                        buffer.write(StringUtilities
                                .getIndentPrefix(extraIndent + 2)
                                + "<property name=\"input\"/>\n");
                    }
                    if (isOutput) {
                        buffer.write(StringUtilities
                                .getIndentPrefix(extraIndent + 2)
                                + "<property name=\"output\"/>\n");
                    }
                    if (ioPort.isMultiport()) {
                        buffer.write(StringUtilities
                                .getIndentPrefix(extraIndent + 2)
                                + "<property name=\"multiport\"/>\n");
                    }
                }
                attributes = port.attributeList();
                for (Attribute attribute : attributes) {
                    if (!_IGNORED_ATTRIBUTES.contains(attribute.getName())) {
                        attribute.exportMoML(buffer, extraIndent + 2);
                    }
                }
                buffer.write(StringUtilities.getIndentPrefix(extraIndent + 1)
                        + "</port>\n");
            }

            List<ComponentEntity> classes = group.classDefinitionList();
            for (ComponentEntity entity : classes) {
                Attribute attribute = entity.getAttribute("_noAutonaming");
                if (attribute != null
                        && ((BooleanToken) ((Parameter) attribute).getToken())
                        .booleanValue()) {
                    entity.exportMoML(buffer, extraIndent + 2);
                }
            }

            List<ComponentEntity> entities = group.entityList();
            for (ComponentEntity entity : entities) {
                Attribute attribute = entity.getAttribute("_noAutonaming");
                if (attribute != null
                        && ((BooleanToken) ((Parameter) attribute).getToken())
                        .booleanValue()) {
                    entity.exportMoML(buffer, extraIndent + 2);
                }
            }

            buffer.write(StringUtilities.getIndentPrefix(extraIndent + 1)
                    + "<group name=\"auto\">\n");

            classes = group.classDefinitionList();
            for (ComponentEntity entity : classes) {
                Attribute attribute = entity.getAttribute("_noAutonaming");
                if (attribute == null
                        || !((BooleanToken) ((Parameter) attribute).getToken())
                        .booleanValue()) {
                    entity.exportMoML(buffer, extraIndent + 2);
                }
            }

            entities = group.entityList();
            for (ComponentEntity entity : entities) {
                Attribute attribute = entity.getAttribute("_noAutonaming");
                if (attribute == null
                        || !((BooleanToken) ((Parameter) attribute).getToken())
                        .booleanValue()) {
                    entity.exportMoML(buffer, extraIndent + 2);
                }
            }

            List<ComponentRelation> relations = group.relationList();
            for (ComponentRelation relation : relations) {
                relation.exportMoML(buffer, extraIndent + 2);
            }

            buffer.write(group.exportLinks(extraIndent + 2, null));
            buffer.write(StringUtilities.getIndentPrefix(extraIndent + 1)
                    + "</group>\n");
            if (after != null || before != null) {
                buffer.write(StringUtilities.getIndentPrefix(extraIndent)
                        + "</group>\n");
            }

            if (after != null) {
                String oldType = null;
                StringParameter typeParameter = (StringParameter) after
                        .getAttribute("_type");
                if (typeParameter == null) {
                    typeParameter = new StringParameter(after, "_type");
                } else {
                    oldType = typeParameter.getExpression();
                }
                typeParameter.setExpression("delayed");
                try {
                    buffer.write(StringUtilities.getIndentPrefix(1)
                            + "<group>\n");
                    after.exportMoML(buffer, 2);
                    buffer.write(StringUtilities.getIndentPrefix(1)
                            + "</group>\n");
                } finally {
                    if (oldType == null) {
                        typeParameter.setContainer(null);
                    } else {
                        typeParameter.setExpression(oldType);
                    }
                }
            }

            buffer.write("</group>\n");

            return buffer.toString();
        } catch (Throwable throwable) {
            // This should not occur.
            throw new InternalErrorException(null, throwable,
                    "Unable to get the " + "Moml content for group "
                            + group.getName() + ".");
        }
    }

    /** Add a parameter and expression to the map of parameters
     *  to override.
     *  @param name The name of the parameter.
     *  @param expression The expression of the parameter.
     */
    public void overrideParameter(String name, String expression) {
        _overriddenParameters.put(name, expression);
    }

    /** The set of attribute names that need to be ignored while generating
     *  the Moml.
     */
    private static final HashSet<String> _IGNORED_ATTRIBUTES = new HashSet<String>();

    private HashMap<String, String> _overriddenParameters = new HashMap<String, String>();

    static {
        _IGNORED_ATTRIBUTES.add("GroupIcon");
        _IGNORED_ATTRIBUTES.add("_alternateGetMomlAction");
        _IGNORED_ATTRIBUTES.add("_createdBy");
        _IGNORED_ATTRIBUTES.add("_designPatternIcon");
        _IGNORED_ATTRIBUTES.add("_hideName");
        _IGNORED_ATTRIBUTES.add("_vergilSize");
        _IGNORED_ATTRIBUTES.add("_vergilZoomFactor");
        _IGNORED_ATTRIBUTES.add("_vergilCenter");
        _IGNORED_ATTRIBUTES.add("_windowProperties");
    }
}
