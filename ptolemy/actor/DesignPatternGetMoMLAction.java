/* An action that generates the Moml for a group of states to be used as a
 design pattern.

 Copyright (c) 2008 The Regents of the University of California.
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
import java.util.HashSet;
import java.util.List;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
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
  @since Ptolemy II 7.1
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
        Attribute before = null;
        Attribute after = null;
        StringWriter buffer = new StringWriter();
        int extraIndent = 0;
        try {
            buffer.write("<group>\n");

            before = object.getAttribute("_transformationBefore");
            if (before != null) {
                new Parameter(before, "_immediate").setToken(BooleanToken.TRUE);
            }

            after = object.getAttribute("_transformationAfter");
            if (after != null) {
                new Parameter(after, "_immediate").setToken(BooleanToken.TRUE);
                extraIndent++;
                buffer.write(StringUtilities.getIndentPrefix(extraIndent) +
                        "<group>\n");
            }

            List<Attribute> attributes = group.attributeList();
            for (Attribute attribute : attributes) {
                if (!_IGNORED_ATTRIBUTES.contains(attribute.getName()) &&
                        (after == null || attribute != after)) {
                    attribute.exportMoML(buffer, extraIndent + 1);
                }
            }

            List<Port> ports = group.portList();
            for (Port port : ports) {
                buffer.write(StringUtilities.getIndentPrefix(extraIndent + 1) +
                        "<port name=\"" + port.getName() + "\">\n");
                if (port instanceof IOPort) {
                    IOPort ioPort = (IOPort) port;
                    boolean isInput = ioPort.isInput();
                    boolean isOutput = ioPort.isOutput();
                    if (isInput) {
                        buffer.write(StringUtilities.getIndentPrefix(
                                extraIndent + 2) +
                                "<property name=\"input\"/>\n");
                    }
                    if (isOutput) {
                        buffer.write(StringUtilities.getIndentPrefix(
                                extraIndent + 2) +
                                "<property name=\"output\"/>\n");
                    }
                    if (ioPort.isMultiport()) {
                        buffer.write(StringUtilities.getIndentPrefix(
                                extraIndent + 2) +
                                "<property name=\"multiport\"/>\n");
                    }
                }
                attributes = port.attributeList();
                for (Attribute attribute : attributes) {
                    if (!_IGNORED_ATTRIBUTES.contains(attribute.getName())) {
                        attribute.exportMoML(buffer, extraIndent + 2);
                    }
                }
                buffer.write(StringUtilities.getIndentPrefix(extraIndent + 1) +
                        "</port>");
            }

            buffer.write(StringUtilities.getIndentPrefix(extraIndent + 1) +
                    "<group name=\"auto\">\n");

            List<ComponentEntity> classes = group.classDefinitionList();
            for (ComponentEntity entity : classes) {
                entity.exportMoML(buffer, extraIndent + 2);
            }

            List<ComponentEntity> entities = group.entityList();
            for (ComponentEntity entity : entities) {
                entity.exportMoML(buffer, extraIndent + 2);
            }

            List<ComponentRelation> relations = group.relationList();
            for (ComponentRelation relation : relations) {
                relation.exportMoML(buffer, extraIndent + 2);
            }

            buffer.write(group.exportLinks(extraIndent + 2, null));
            buffer.write(StringUtilities.getIndentPrefix(extraIndent + 1) +
                    "</group>\n");
            buffer.write(StringUtilities.getIndentPrefix(extraIndent) +
                    "</group>\n");

            if (after != null) {
                buffer.write(StringUtilities.getIndentPrefix(extraIndent) +
                        "<group>\n");
                after.exportMoML(buffer, extraIndent + 1);
                buffer.write(StringUtilities.getIndentPrefix(extraIndent) +
                        "</group>\n");
                buffer.write("</group>\n");
            }

            return buffer.toString();
        } catch (Exception e) {
            // This should not occur.
            throw new InternalErrorException(null, e, "Unable to get the " +
                    "Moml content for group " + group.getName() + ".");
        } finally {
            if (before != null) {
                Attribute attribute = before.getAttribute("_immediate");
                if (attribute != null) {
                    try {
                        attribute.setContainer(null);
                    } catch (Throwable t) {
                        // Ignore.
                    }
                }
            }
            if (after != null) {
                Attribute attribute = after.getAttribute("_immediate");
                if (attribute != null) {
                    try {
                        attribute.setContainer(null);
                    } catch (Throwable t) {
                        // Ignore.
                    }
                }
            }
        }
    }

    /** The set of attribute names that need to be ignored while generating
     *  the Moml.
     */
    private static final HashSet<String> _IGNORED_ATTRIBUTES =
        new HashSet<String>();

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
