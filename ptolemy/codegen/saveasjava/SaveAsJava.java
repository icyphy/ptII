/* A code generator for each actor in a system.

 Copyright (c) 2000-2001 The Regents of the University of California.
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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.codegen.saveasjava;

import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringUtilities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
//////////////////////////////////////////////////////////////////////////
//// SaveAsJava
/**
This class constructs standalone Java specifications of Ptolemy II
models.
@author Shuvra S. Bhattacharyya and Edward A. Lee
@version $Id$
*/

class SaveAsJava {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the java code associated with a top level Ptolemy II
     *  object and all of its descendants.  The generated code
     *  is placed in the top package, that is, the code does not
     *  contain a <code>package</code> directive.
     *  @param toplevel The root object of the topology to be saved.
     *  @return The generated java code.
     */
    public String generate(NamedObj toplevel) throws IllegalActionException {
	return generate(toplevel, "");
    }

    /** Return the java code associated with a top level Ptolemy II
     *  object and all of its descendants.  The generated code
     *  is placed in the package specified by the packageName parameter.
     *  @param toplevel The root object of the topology to be saved.
     *  @param packageName The java package to generate the java code in.
     *  If this parameter is non-null and non-empty, then a 
     *  <code>package <i>packageName</i>;</code> line is added to the
     *  generated code.
     *  @return The generated java code.
     */
    public String generate(NamedObj toplevel, String packageName)
	throws IllegalActionException {
        // Data associated with a given class under consideration
        String className = toplevel.getClass().getName();

        // String to hold the generated Java code
        String code = new String();

        // String to hold the import declarations for the generated code
        String importCode = new String();

        // More specific representation of the root topology object
        CompositeEntity compositeModel;

        // Initialize the list of classes to import in the generated
        // Java code.
        _importList = new LinkedList();

        // Check that the argument is a composite entity.
        if (!(toplevel instanceof CompositeEntity)) {
            throw new IllegalActionException(toplevel,
                    "SaveAsJava feature only operates on composite entities");
        }
        compositeModel = (CompositeEntity)toplevel;

        // Generate class header output.
	// We call the class we are generating CGFoo so that it
	// will not collide with Foo.
        String sanitizedName = "CG" + sanitizeName(compositeModel);
        if (sanitizedName.length() == 0 ) {
            throw new IllegalActionException("Name of the toplevel entity is "
                    + "empty, so we don't know what file to build in.");
        }

        code += "public class "
            + sanitizedName
            + " extends "
            + _getClassName(compositeModel)
            + " {\n\n" + _indent(1)
            + "public " + sanitizedName
            + "(Workspace w) throws"
            + " IllegalActionException {\n" + _indent(2)
            + "super(w);\n\n" + _indent(2) + "try {\n";

        // When exporting MoML, we want to identify the class
        // as the parent class rather than this class so that
        // this class need not be present to instantiate the
        // model.
        code += _indent(2)
            + "getMoMLInfo().className = \""
            + _getClassName(compositeModel)
            + "\";\n";

        // Generate attributes.
        code += _generateAttributes(compositeModel);

        // Generate code to instantiate
        // and connect the system components.
        code += _generateComponents(compositeModel);

        // Generate trailer code for the
        // composite actor constructor
        code += _indent(2)
            + "} catch (NameDuplicationException e) {\n"
            + _indent(3)
            + "throw new RuntimeException(e.toString());\n"
            + _indent(2) + "}\n"
            + _indent(1) + "}\n"
            + "}\n";

        // Generate statements for importing classes.
        _insertIfUnique(
                "ptolemy.kernel.util.Workspace",
                _importList);
        _insertIfUnique(
                "ptolemy.kernel.util.IllegalActionException",
                _importList);
        _insertIfUnique(
                "ptolemy.kernel.util.NameDuplicationException",
                _importList);

        try {
            Iterator iter = _importList.iterator();
            while (iter.hasNext()) {
                String p = (String)(iter.next());
                importCode += "import " + p + ";\n";
            }
            code = importCode + "\n" + code;
        } catch (Exception ex) {
            throw new IllegalActionException(ex.getMessage()
                    + "Exception raised while creating the "
                    + "import list '"
                    + importCode + "'.\n");
        }

	if (packageName != null && packageName != "") {
	    // Add the package statement to the top.
	    code = "package " + packageName + ";\n\n" + code;
	}

        return code;
    }

    // Sanitize the name of the specified object so that it is a
    // proper Java identifier. In particular, dots are replaced with
    // underscore, spaces are replaced with underscore, and any non
    // alphanumeric is replaced with X.  This method ensures that
    // returned name is always unique by appending a numeric if
    // necessary.
    public String sanitizeName(Nameable object) {
        String name = (String)_nameMap.get(object);
        if(name == null) {
            NamedObj toplevel = ((NamedObj)object).toplevel();
            String nameToSanitize;
            if (object == toplevel) {
                nameToSanitize = toplevel.getName();
            } else {
                nameToSanitize = ((NamedObj)object).getName(toplevel);
            }

            // FIXME: Only replacing dots, spaces, commas and colons for now.
            name = nameToSanitize.replace('.', '_');
            name = name.replace(' ', '_');
            name = name.replace(',', '_');
            name = name.replace(':', '_');


            // If the name begins with a number, add a leading _
            switch (name.charAt(0)) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                name = "_" + name;
                break;
            default:
                break;
            }

	    // Replace "=" with "Equals"
	    name = StringUtilities.substitute(name, "=", "Equals");

            _nameMap.put(object, name);
        }
        return name;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Generate Java code that creates attributes, if necessary,
     *  and initializes them with their initial values.
     *  Since attributes can themselves have attributes, this
     *  routine is recursive.
     *  @param object The object with attributes.
     *  @return The Java code defining attributes for the specified
     *   object.
     */
    protected String _generateAttributes(NamedObj object) {
        StringBuffer result = new StringBuffer();
        Iterator attributes = object.attributeList().iterator();
        // The name of the object when it is referenced as a container
        // of an attribute in the generated code.
        String nameAsContainer;
        if (object.getContainer() == null) nameAsContainer = "this";
        else nameAsContainer = sanitizeName(object);

        while (attributes.hasNext()) {
            Attribute attribute = (Attribute)attributes.next();

            // First, check to see whether the attribute is persistent
            // by exporting MoML and seeing whether the result is empty.
            // FIXME: This seems like the wrong way to determine that
            // the attribute is transient.
            String moml = attribute.exportMoML();
            if (moml.length() > 0) {
                if (!_isPublicMember(attribute, object)) {
                    String attributeClass = _getClassName(attribute);
                    String attributeName = sanitizeName(attribute);
                    result.append(_indent(3));
                    result.append(attributeClass);
                    result.append(" ");
                    result.append(attributeName);
                    result.append(" = new ");
                    result.append(attributeClass);
                    result.append("(");
                    result.append(nameAsContainer);
                    result.append(", \"");
                    result.append(attributeName);
                    result.append("\");");
                    result.append("\n");
                }

                if (attribute instanceof Settable) {
                    String expression = ((Settable)attribute).getExpression();
                    if (expression != null) {
                        result.append(_indent(3));
                        result.append(sanitizeName(attribute));
                        result.append(".setExpression(\"");
                        result.append(_escapeQuotes(expression));
                        result.append("\");\n");
                    }
                }
                // Recursively generate any nested attributes
                result.append(_generateAttributes(attribute));
            }
        }
        return result.toString();
    }

    /** Generate Ptolemy II Java code to instantiate a component,
     *  and for a composite component, the code to instantiate and
     *  connect all components that are nested within the component.
     *
     *  @param model The component for which code is to be generated.
     *  @return The generated Ptolemy II Java code for implementing
     *   the component.
     */
    protected String _generateComponents(ComponentEntity model)
            throws IllegalActionException {
        // FIXME: Use StringBuffer, not String.
        String code = new String();
        String className = _getClassName(model);
        String sanitizedName = sanitizeName(model);
        CompositeEntity container;
        String containerName;

        // The name of <model> when it is referenced as a container
        // of another entity.
        String nameAsContainer;

        if ((container = ((CompositeEntity)(model.getContainer()))) != null) {
            if (container.getContainer() == null) {
                containerName = "this";
            } else {
                containerName = sanitizeName(container);
            }
            nameAsContainer = sanitizedName;
            code += _indent(3)
                + className
                + " "
                + sanitizedName
                + " = new "
                + className
                + "("
                + containerName
                + ", \""
                + sanitizedName
                + "\");\n";
            code += _generatePorts(model);
            code += _generateAttributes(model);
        }
        else nameAsContainer = "this";

        if (!model.isAtomic()) {

            // Instantiate the actors inside the composite actor
            Iterator components = ((CompositeEntity)model)
                .entityList().iterator();
            while (components.hasNext()) {
                code += _generateComponents((ComponentEntity)
                        (components.next()));
            }

            // Instantiate the connections between actors
            Iterator relations = ((CompositeEntity)model)
                .relationList().iterator();
            while (relations.hasNext()) {
                Relation relation = (Relation)(relations.next());
                Iterator ports = relation.linkedPortList().iterator();

                String relationAttributes = _generateAttributes(relation);

                // We adopt the convention of using the "connect"
                // method for relations that are incident to exactly
                // two links, and that have no attributes for which
                // code needs to be generated.
                if ((relation.numLinks() == 2) &&
                        (relationAttributes.length() == 0)) {
                    Port port1 = (Port) ports.next();
                    Port port2 = (Port) ports.next();
                    code += _indent(3);
                    if (container != null) {
                        code += sanitizedName + ".";
                    }
                    code += "connect ("
                        + sanitizeName(port1)
                        + ", "
                        + sanitizeName(port2)
                        + ");\n";
                }
                // Explicitly instantiate the relation,
                // generate code to set relevant attributes of the
                // relation, and generate
                // a link() call for each port associated with the relation.
                else {
                    String relationClassName = _getClassName(relation);
                    code += _indent(3)
                        + relationClassName
                        + " "
                        + sanitizeName(relation);
                    code += " = new "
                        + relationClassName
                        + "("
                        + nameAsContainer;
                    code += ", \""
                        + sanitizeName(relation)
                        + "\");\n";
                    code += relationAttributes;
                    while (ports.hasNext()) {
                        Port p = (Port)ports.next();
                        code += _indent(3)
                            + sanitizeName(p)
                            + ".link("
                            + sanitizeName(relation)
                            + ");\n";
                    }
                }
            }

        }

        return code;

    }

    /** Generate Java code that defines ports, if necessary, and
     *  that sets up the attributes of each port.
     *  Ports are defined if they are not public members of the specified
     *  component.
     *  @param component The component with ports.
     *  @return The Java code defining ports for the specified
     *   component.
     */
    protected String _generatePorts(Entity component) {
        StringBuffer result = new StringBuffer();
        Iterator ports = component.portList().iterator();
        // The name of the component when it is referenced as a container
        // of an attribute in the generated code.
        String nameAsContainer;

        if (component.getContainer() == null) nameAsContainer = "this";
        else nameAsContainer = sanitizeName(component);

        while (ports.hasNext()) {
            Port port = (Port)ports.next();
            // Create an instance of the port if there
            // is no matching public member.
            if (!_isPublicMember(port, component)) {
                // Port does not appear as a public member.
                String portClass = _getClassName(port);
                String portName = sanitizeName(port);
                result.append(_indent(3));
                result.append(portClass);
                result.append(" ");
                result.append(portName);
                result.append(" = new ");
                result.append(portClass);
                result.append("(");
                result.append(nameAsContainer);
                result.append(", \"");
                result.append(portName);
                result.append("\");");
                result.append("\n");
            }

            // Generate the attributes of the port
            result.append(_generateAttributes(port));
        }
        return result.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Given a string, replace all quotation marks with escaped
     *  quotation marks, all backslashes with double backslashes,
     *  and all \n characters with "\n + ".
     *  This is because the Java compiler interprets quotation marks
     *  as closing the string, and backslashes as escaping special
     *  characters.
     *  @param string The string to escape.
     *  @return A new string with quotation marks replaced.
     */
    private static String _escapeQuotes(String string) {
        string = StringUtilities.substitute(string, "\\", "\\\\");
        string = StringUtilities.substitute(string, "\"", "\\\"");
	String lineSeparator = System.getProperty("line.separator");
	// Convert line Separators to "\n + "
        string =
	    StringUtilities.substitute(string, "\n",
				       "\"" + lineSeparator + " + \""); 
        return string;
    }

    //  Insert the specified name into the specified list
    //  if the name does not already exist in the list.
    private void _insertIfUnique(String name, LinkedList list)
            throws IndexOutOfBoundsException {
        if (!list.contains(name)) {
            list.add(0, name);
        }
    }

    // Return a string that generates an indentation string (a sequence
    // of spaces) for a given indentation level. Each indentation
    // level is four characters wide.
    private String _indent(int level) {
        String indent = new String();
        int i;
        for (i = 0; i < level; i++) {
	    indent += "    ";
	}
        return indent;
    }

    // Return the class name associated with a named object. Also,
    // if it does not already exist in the list of classes to be
    // imported in the generated Java code, insert the class name
    // (fully qualified) into this list.
    private String _getClassName(NamedObj object) {
        // We use fully qualified classnames so that we can
        // generate code for actor/lib/test/auto/IIR.xml.
        // If we don't, then when we create IIR.java, we have problems
        // compiling because we have the ptolemy.actor.lib.IIR actor.

        String classFullName = object.getClass().getName();
        return classFullName;
    }

    // Return TRUE if and only if <obj> is a accessible as
    // a public member of its container, <container>.
    // If it is a public member, then enter it into the
    // name table as X.Y, where X is the fully-qualified name
    // of the container of the object, and Y is the name of the object.
    private boolean _isPublicMember(NamedObj obj, NamedObj container) {
        try {
            // If the following succeeds, then there is a public
            // field whose name matches that of the object.
            container.getClass().getField(obj.getName());
            // Henceforth, refer to this object as
            // containerName.objectName.
            _nameMap.put(obj,
                    sanitizeName(container)
                    + "."
                    + obj.getName());
        } catch (NoSuchFieldException ex) {
            return false;
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // The list of classes to import in the generated Java code.
    LinkedList _importList;

    // A table of names of objects.
    Map _nameMap = new HashMap();
}
