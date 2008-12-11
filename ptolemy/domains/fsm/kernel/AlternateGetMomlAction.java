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
package ptolemy.domains.fsm.kernel;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// AlternateGetMomlAction

/**
 An action that generates the Moml for a group of states to be used as a
 design pattern.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class AlternateGetMomlAction {

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
        if (object instanceof Group) {
            CompositeEntity group = (CompositeEntity) object;
            StringWriter buffer = new StringWriter();
            try {
                List<Attribute> attributes = group.attributeList();
                for (Attribute attribute : attributes) {
                    if (!_IGNORED_ATTRIBUTES.contains(attribute.getName())) {
                        attribute.exportMoML(buffer, 0);
                    }
                }

                List<Port> ports = group.portList();
                for (Port port : ports) {
                    buffer.write("<port name=\"" + port.getName() + "\">\n");
                    if (port instanceof IOPort) {
                        IOPort ioPort = (IOPort) port;
                        boolean isInput = ioPort.isInput();
                        boolean isOutput = ioPort.isOutput();
                        if (isInput) {
                            buffer.write(StringUtilities.getIndentPrefix(1) +
                                    "<property name=\"input\"/>\n");
                        }
                        if (isOutput) {
                            buffer.write(StringUtilities.getIndentPrefix(1) +
                                    "<property name=\"output\"/>\n");
                        }
                        if (ioPort.isMultiport()) {
                            buffer.write(StringUtilities.getIndentPrefix(1) +
                                    "<property name=\"multiport\"/>\n");
                        }
                    }
                    attributes = port.attributeList();
                    for (Attribute attribute : attributes) {
                        if (!_IGNORED_ATTRIBUTES.contains(attribute.getName())) {
                            attribute.exportMoML(buffer, 1);
                        }
                    }
                    buffer.write("</port>");
                }

                buffer.write("<group name=\"auto\">\n");

                List<ComponentEntity> classes = group.classDefinitionList();
                for (ComponentEntity entity : classes) {
                    entity.exportMoML(buffer, 1);
                }

                List<ComponentEntity> entities = group.entityList();
                for (ComponentEntity entity : entities) {
                    entity.exportMoML(buffer, 1);
                }

                List<ComponentRelation> relations = group.relationList();
                for (ComponentRelation relation : relations) {
                    relation.exportMoML(buffer, 1);
                }

                buffer.write(group.exportLinks(1, null));
                buffer.write("</group>\n");
                return buffer.toString();
            } catch (IOException e) {
                // This should not occur.
                throw new InternalErrorException(null, e, "Unable to get the " +
                        "Moml content for group " + group.getName() + ".");
            }
        } else {
            return object.exportMoML(name);
        }
    }

    /** The set of attribute names that need to be ignored while generating the
     *  Moml.
     */
    private static final HashSet<String> _IGNORED_ATTRIBUTES =
        new HashSet<String>();

    static {
        _IGNORED_ATTRIBUTES.add("GroupIcon");
        _IGNORED_ATTRIBUTES.add("_hideName");
    }
}
