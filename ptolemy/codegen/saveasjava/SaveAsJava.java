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

import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Relation;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.util.LinkedList;
import java.util.Iterator;

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
        String clfullname;
        Class cl = toplevel.getClass();
        String clname = cl.getName();  

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
        code += "public class " + compositeModel.getName() 
                + " extends "
                + _getClassName(compositeModel)
                + " {\n\n" + _indent(1)
                + "public " + compositeModel.getName() 
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

            // FIXME: For now, creating new attributes always.
            // Really should check to see whether the component has
            // a public member whose name and type match the attribute,
            // and if so, just set that.

            String attributeClass = _getClassName(attribute);
            result.append(_indent(3));
            result.append(attributeClass);
            result.append(" ");
            result.append(attribute.getName());
            result.append(" = new ");
            result.append(attributeClass);
            result.append("(this, \"");
            result.append(attribute.getName());
            result.append("\");");
            result.append("\n");
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
        String code = new String(); 
        String clname = _getClassName(model);

        if (model.isAtomic()) {
             code += _indent(3)
                     + clname + " " + model.getName() + " = new "
                     + clname + "(this, \"" + model.getName()
                             + "\");\n";
        }
        else {

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
                     Port port1 = (Port) ports.next();
                     Port port2 = (Port) ports.next();
                     code += _indent(3);
                     code += "connect (" + port1.getContainer().getName()
                     + "." + port1.getName() + ", "
                     + port2.getContainer().getName() + "." + port2.getName()
                     + ");\n";
                 }
                 // Explicitly instantiate the relation, and generate 
                 // a link() call for each port associated with the relation.
                 else {
                     code += _indent(3)
                             + "TypedIORelation "
                             + relation.getName();
                     code += " = new TypedIORelation(" + "this";
                     code += ", \"" + relation.getName() + "\");\n";
                     while (ports.hasNext()) {
                         Port p = (Port)ports.next();
                         code += _indent(3);
                         code += p.getContainer().getName() + ".";
                         code += p.getName() + ".link(";
                         code += relation.getName() + ");\n"; 
                     }
                 }
             }
           
        }
    
        return code;
       
    }

    /** Split the specified name at the last period, and return the
     *  last part. If there is no period, return the original name.
     *  @param name The string from which the suffix is to be extracted.
     *  @return The extracted suffix.
     */
     
    protected static final String _extractSuffix(String name) {
        return name.substring(name.lastIndexOf(".") + 1);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

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
            String clname = _extractSuffix(clfullname);
            _insertIfUnique(clfullname, _importList);
            return clname;
    }
        

    // The list of classes to import in the generated Java code.
    LinkedList _importList;

}
