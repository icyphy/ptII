/* A GUI widget for configuring ports.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.actor.TypedIOPort;
import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.StringUtilities;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.BoxLayout;

//////////////////////////////////////////////////////////////////////////
//// PortConfigurer
/**
This class is an editor to configure the ports of an object.
It supports setting their input, output, and multiport properties,
and adding and removing ports.  Only ports that extend the TypedIOPort
class are listed, since more primitive ports cannot be configured
in this way.

@see Configurer
@author Steve Neuendorffer and Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
@deprecated This class is no longer used.  Use PortConfigurerDialog.
*/

public class PortConfigurer extends Query implements QueryListener {

    /** Construct a port configurer for the specified entity.
     *  @param object The entity to configure.
     */
    public PortConfigurer(Entity object) {
        super();
        this.addQueryListener(this);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        setTextWidth(15);

        // The second column is for type designators.
        setColumns(3);

        _object = object;

        Iterator ports = _object.portList().iterator();
        while (ports.hasNext()) {
            Object candidate = ports.next();
            if (candidate instanceof TypedIOPort) {
                TypedIOPort port = (TypedIOPort)candidate;
                Set optionsDefault = new HashSet();
                if (port.isInput()) optionsDefault.add("input");
                if (port.isOutput()) optionsDefault.add("output");
                if (port.isMultiport()) optionsDefault.add("multiport");

                addSelectButtons(port.getName(), port.getName(),
                        _optionsArray, optionsDefault);

                String typeEntryName = port.getName() + " type";
                addLine(typeEntryName, typeEntryName,
                        port.getType().toString());

                // Add a column that controls on which side
                // of the icon the port lies.
                StringAttribute cardinal
                    = (StringAttribute)port.getAttribute("_cardinal");
                String cardinalValue = "SOUTH";
                if (cardinal != null) {
                    cardinalValue = cardinal.getExpression().toUpperCase();
                } else if (port.isInput() && !port.isOutput()) {
                    cardinalValue = "WEST";
                } else if (port.isOutput() && !port.isInput()) {
                    cardinalValue = "EAST";
                }
                addChoice(port.getName() + " cardinal",
                        port.getName() + ": cardinal direction",
                        _cardinals, cardinalValue);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Apply the changes by configuring the ports that have changed.
     */
    public void apply() {
        StringBuffer moml = new StringBuffer("<group>");
        boolean foundOne = false;
        Iterator ports = _object.portList().iterator();
        NamedObj parent = null;
        while (ports.hasNext()) {
            Object candidate = ports.next();
            if (candidate instanceof TypedIOPort) {
                TypedIOPort port = (TypedIOPort)candidate;
                String name = port.getName();

                // Check whether the positioning information has changed.
                String nameCardinal = name + " cardinal";
                if (_changed.contains(nameCardinal)) {

                    // The context for the MoML should be the first container
                    // above this port in the hierarchy that defers its
                    // MoML definition, or the immediate parent
                    // if there is none.
                    parent = MoMLChangeRequest.getDeferredToParent(port);
                    if (parent == null) {
                        parent = (NamedObj)port.getContainer();
                    }
                    foundOne = true;
                    moml.append("<port name=\"");
                    moml.append(port.getName(parent));
                    moml.append("\">");

                    String cardinalVal = stringValue(nameCardinal);
                    moml.append(
                            "<property name=\"_cardinal\" "
                            + "class = \"ptolemy.kernel.util.StringAttribute\" "
                            + "value = \""
                            + cardinalVal
                            + "\"/>");
                    moml.append("</port>");
                }

                // If either the I/O designation or the type changed,
                // then generate additional MoML.
                String typeEntryName = name + " type";
                if (_changed.contains(name)
                        || _changed.contains(typeEntryName)) {
                    // Change to the type or I/O status.  Create a MoML command.
                    String value = getStringValue(name);

                    // First, parse the value, which may be a
                    // comma-separated list.
                    Set selectedValues = new HashSet();
                    StringTokenizer tokenizer = new StringTokenizer(value, ",");
                    while (tokenizer.hasMoreTokens()) {
                        selectedValues.add(tokenizer.nextToken().trim());
                    }

                    // The context for the MoML should be the first container
                    // above this port in the hierarchy that defers its
                    // MoML definition, or the immediate parent
                    // if there is none.
                    parent = MoMLChangeRequest.getDeferredToParent(port);
                    if (parent == null) {
                        parent = (NamedObj)port.getContainer();
                    }
                    foundOne = true;
                    moml.append("<port name=\"");
                    moml.append(port.getName(parent));
                    moml.append("\">");

                    if (selectedValues.contains("input")) {
                        moml.append("<property name=\"input\"/>");
                    } else {
                        moml.append(
                                "<property name=\"input\" value=\"false\"/>");
                    }
                    if (selectedValues.contains("output")) {
                        moml.append("<property name=\"output\"/>");
                    } else {
                        moml.append(
                                "<property name=\"output\" value=\"false\"/>");
                    }
                    if (selectedValues.contains("multiport")) {
                        moml.append("<property name=\"multiport\"/>");
                    } else {
                        moml.append(
                                "<property name=\"multiport\" value=\"false\"/>");
                    }

                    if (_changed.contains(typeEntryName)) {
                        // Type designation has changed.
                        String type = getStringValue(typeEntryName);
                        moml.append(
                                "<property name=\"_type\" "
                                + "class = \"ptolemy.actor.TypeAttribute\" "
                                + "value = \""
                                + StringUtilities.escapeForXML(type)
                                + "\"/>");
                    }
                    moml.append("</port>");
                }
            }
        }

        if (foundOne) {
            moml.append("</group>");
            MoMLChangeRequest request = new MoMLChangeRequest(
                    this,            // originator
                    parent,          // context
                    moml.toString(), // MoML code
                    null);           // base

            request.setUndoable(true);

            // NOTE: There is no need to listen for completion
            // or errors in this change request, since, in theory,
            // it will just work.  Will someone report the error
            // if one occurs?  I hope so...
            parent.requestChange(request);
        }
    }

    /** Called to notify that one of the entries has changed.
     *  This simply sets a flag that enables application of the change
     *  when the apply() method is called.
     *  @param name The name of the entry that changed.
     */
    public void changed(String name) {
        _changed.add(name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Possible placements of ports.
    private String[] _cardinals = {"NORTH", "SOUTH", "EAST", "WEST" };

    // The set of names of ports that have changed.
    private Set _changed = new HashSet();

    // The object that this configurer configures.
    private Entity _object;

    // The possible configurations for a port.
    private String[] _optionsArray = {"input", "output", "multiport"};
}
