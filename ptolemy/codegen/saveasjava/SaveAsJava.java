/* A code generator for each actor in a system.

 Copyright (c) 2000 The Regents of the University of California.
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

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// SaveAsJava
/**
This class constructs standalone Java specifications of Ptolemy II
models.
@author Shuvra S. Bhattacharyya
@version $Id$ 
*/

class SaveAsJava {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the java code associated with a top level Ptolemy II
     *  object and all of its descendants. 
     *  @param toplevel The root object of the topology to be saved.
     *  @return The generated java code.
     */
    public String generate(NamedObj toplevel) throws IllegalActionException {

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
            "SavaAsJava feature only operates on composite entities");
        }
        compositeModel = (CompositeEntity)toplevel;

        // Generate class header output
        String sanitizedName = _name(compositeModel);
        code += "public class "
                + sanitizedName
                + " extends "
                + _getClassName(compositeModel)
                + " {\n\n" + _indent(1)
                + "public " + sanitizedName 
                + "(Workspace w) throws"
                + " IllegalActionException {\n" + _indent(2) 
                + "super(w);\n\n" + _indent(2) + "try {\n";

        // Generate attributes.
        code += _generateAttributes(compositeModel);

        // Generate code to instantiate and connect the system components
        code += _generateComponents(compositeModel);

        // Generate trailer code for the composite actor constructor
        code +=  
        _indent(2) + "} catch (NameDuplicationException e) {\n" +
        _indent(3) + "throw new RuntimeException(e.toString());\n" +
        _indent(2) + "}\n" +
        _indent(1) + "}\n" +
        "}\n";
         
        // Generate statements for importing classes.
        _insertIfUnique("ptolemy.kernel.util.Workspace", _importList);
        _insertIfUnique("ptolemy.kernel.util.IllegalActionException", 
                        _importList);
        _insertIfUnique("ptolemy.kernel.util.NameDuplicationException",
                        _importList);
        _insertIfUnique("ptolemy.actor.TypedIORelation", _importList);
        try {
            Iterator iter = _importList.iterator();
            while (iter.hasNext()) {
                String p = (String)(iter.next());
                importCode += "import " + p + ";\n";
            }
            code = importCode + "\n" + code; 
        } catch (Exception ex) {
           throw new IllegalActionException(ex.getMessage()
           + "Exception raised while creating the import list '" 
           + importCode + "'.\n");
        }
  
        return code;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Split the specified name at the last period, and return the
     *  last part. If there is no period, return the original name.
     *  @param name The string from which the suffix is to be extracted.
     *  @return The extracted suffix.
     */
    protected static final String _extractSuffix(String name) {
        return name.substring(name.lastIndexOf(".") + 1);
    }

    /** Generate Java code that creates attributes, if necessary,
     *  and initializes them with their initial values.
     *  @param component The component with attributes.
     *  @return The Java code defining attributes for the specified
     *   component.
     */
    protected String _generateAttributes(NamedObj component) {
        StringBuffer result = new StringBuffer();
        Iterator attributes = component.attributeList().iterator();
        while (attributes.hasNext()) {
            Attribute attribute = (Attribute)attributes.next();

            // First, check to see whether the attribute is persistent
            // by exporting MoML and seeing whether the result is empty.
            // FIXME: This seems like the wrong way to determine that
            // the attribute is transient.
            String moml = attribute.exportMoML();
            if (moml.length() > 0) {
                // Create an instance of the attribute.
                // Don't do this if there is a matching public member.
                try {
                    // If the following succeeds, then there is a public
                    // field whose name matches that of the attribute.
                    component.getClass().getField(attribute.getName());
                    // Henceforth, refer to this attribute as
                    // containerName.attributeName.
                    _nameTable.put(attribute,
                            _name(component)
                            + "."
                            + attribute.getName());
                } catch (NoSuchFieldException ex) {
                    String attributeClass = _getClassName(attribute);
                    String attributeName = _name(attribute);
                    result.append(_indent(3));
                    result.append(attributeClass);
                    result.append(" ");
                    result.append(attributeName);
                    result.append(" = new ");
                    result.append(attributeClass);
                    result.append("(this, \"");
                    result.append(attribute.getName());
                    result.append("\");");
                    result.append("\n");
                }

                if (attribute instanceof Settable) {
                    String expression = ((Settable)attribute).getExpression();
                    if (expression != null) {
                        result.append(_indent(3));
                        result.append(_name(attribute));
                        result.append(".setExpression(\"");
                        result.append(_escapeQuotes(expression));
                        result.append("\");\n");
                    }
                }
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
        String sanitizedName = _name(model);
        if (model.getContainer() != null) {
             code += _indent(3)
                     + className
                     + " "
                     + sanitizedName
                     + " = new "
                     + className
                     + "(this, \""
                     + sanitizedName
                     + "\");\n";
             code += _generatePorts(model);
             code += _generateAttributes(model);
        }
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

                 // We adopt the convention of using the "connect"
                 // method for relations that are incident to exactly
                 // two links.
                 if (relation.numLinks() == 2) {
                     // FIXME: The port may not be accessible as a public
                     // member.
                     Port port1 = (Port) ports.next();
                     Port port2 = (Port) ports.next();
                     code += _indent(3);
                     code += "connect ("
                             + _name(port1)
                             + ", "
                             + _name(port2)
                             + ");\n";
                 }
                 // Explicitly instantiate the relation, and generate 
                 // a link() call for each port associated with the relation.
                 else {
                     code += _indent(3)
                             + "TypedIORelation "
                             + _name(relation);
                     code += " = new TypedIORelation("
                             + "this";
                     code += ", \""
                             + _name(relation)
                             + "\");\n";
                     while (ports.hasNext()) {
                         Port p = (Port)ports.next();
                         code += _indent(3)
                                 + _name(p.getContainer())
                                 + "."
                                 + _name(p)
                                 + ".link("
                                 + _name(relation)
                                 + ");\n"; 
                     }
                 }
             }
           
        }
    
        return code;
       
    }

    /** Generate Java code that defines ports, if necessary.
     *  Ports are defined if they are not public members of the specified
     *  component.
     *  @param component The component with ports.
     *  @return The Java code defining ports for the specified
     *   component.
     */
    protected String _generatePorts(Entity component) {
        StringBuffer result = new StringBuffer();
        Iterator ports = component.portList().iterator();
        while (ports.hasNext()) {
            Port port = (Port)ports.next();

            // Create an instance of the port if there
            // is no matching public member.
            try {
                // If the following succeeds, then there is a public
                // field whose name matches that of the port.
                component.getClass().getField(port.getName());
                // Henceforth, refer to this attribute as
                // containerName.portName.
                _nameTable.put(port,
                        _name(component)
                        + "."
                        + port.getName());
            } catch (NoSuchFieldException ex) {
                // Port does not appear as a public member.
                String portClass = _getClassName(port);
                String portName = _name(port);
                result.append(_indent(3));
                result.append(portClass);
                result.append(" ");
                result.append(portName);
                result.append(" = new ");
                result.append(portClass);
                result.append("(this, \"");
                result.append(port.getName());
                result.append("\");");
                result.append("\n");
            }
        }
        return result.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Given a string, replace all quotation marks with escaped
     *  quotation marks.
     *  @param string The string to escape.
     *  @return A new string with quotation marks replaced.
     */
    private static String _escapeQuotes(String string) {
        string = StringUtilities.substitute(string, "\"", "\\\"");
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
            String clfullname = object.getClass().getName();
            String className = _extractSuffix(clfullname);
            _insertIfUnique(clfullname, _importList);
            return className;
    }

    // Sanitize the name of the specified object so that it is a
    // proper Java identifier. In particular, dots are replaced with
    // underscore, spaces are replaced with underscore, and any non
    // alphanumeric is replaced with X.  This method ensures that
    // returned name is always unique by appending a numeric if
    // necessary.
    private String _name(Nameable object) {
        String name = (String)_nameTable.get(object);
        if(name == null) {
            NamedObj toplevel = ((NamedObj)object).toplevel();
            String nameToSanitize;
            if (object == toplevel) {
                nameToSanitize = toplevel.getName();
            } else {
                nameToSanitize = ((NamedObj)object).getName(toplevel);
            }
            // FIXME: Only replacing dots for now.
            name = nameToSanitize.replace('.', '_');
            _nameTable.put(object, name);
        }
        return name;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // The list of classes to import in the generated Java code.
    LinkedList _importList;

    // A table of names of objects.
    Map _nameTable = new HashMap();

}
