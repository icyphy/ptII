/*  A class that generates code for the C structure corresponding to
    an instance of a class (an Object).

    Copyright (c) 2003-2005 The University of Maryland.
    All rights reserved.
    Permission is hereby granted, without written agreement and without
    license or royalty fees, to use, copy, modify, and distribute this
    software and its documentation for any purpose, provided that the above
    copyright notice and the following two paragraphs appear in all copies
    of this software.

    IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
    FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
    ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
    THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
    SUCH DAMAGE.

    THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
    INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
    MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
    PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
    MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
    ENHANCEMENTS, OR MODIFICATIONS.

*/
package ptolemy.copernicus.c;

import soot.ArrayType;
import soot.Modifier;
import soot.SootClass;
import soot.SootField;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;


//////////////////////////////////////////////////////////////////////////
//// ClassStructureGenerator

/**
   A class that generates code for the C structure corresponding to an
   instance of a class (an Object).

   @author Ankush Varma
   @version $Id$
   @since Ptolemy II 4.0
   @Pt.ProposedRating Red (<your email address>)
   @Pt.AcceptedRating Red (ssb)
*/
public class InstanceStructureGenerator extends CodeGenerator {
    /** Default constructor.
     * @param context The context to use for the generated code.
     */
    public InstanceStructureGenerator(Context context) {
        _context = context;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate the structure corresponding to a class instance (an
     * object).
     * @param source The class.
     * @return The code for the C structure corresponding to a instance of
     * this class.
     */
    public String generate(SootClass source) {
        StringBuffer code = new StringBuffer();
        String typeName = CNames.instanceNameOf(source);

        _doneFields = new HashSet();

        // Generate the type declaration header for the class instance
        // structure.
        code.append(_comment("Structure that implements instances of Class "
                            + source.getName()));
        code.append("struct " + typeName + " {\n");

        // Pointer to common, class-specific information.
        code.append("\n" + _indent(1) + CNames.classNameOf(source)
                + " class;\n");

        // Extract the non-static fields, and insert them into the struct
        // that is declared to implement the class.
        Iterator superClasses = _getSuperClasses(source).iterator();

        while (superClasses.hasNext()) {
            SootClass superClass = (SootClass) superClasses.next();
            code.append(_generateInheritedFields(source, superClass));
        }

        code.append(_generateFields(source));

        // Terminator for declared type for class instances.
        code.append("\n};\n\n");

        return code.toString();
    }

    /** Get the map of types required by the generated class.
     * @return The _requiredTypeMap structure created while generating this
     * class.
     */
    public HashMap getRequiredTypeMap() {
        return _requiredTypeMap;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public fields                     ////
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    ///////////////////////////////////////////////////////////////////
    ////                       protected fields                    ////
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Generate a C declaration that corresponds to a class field.
    private String _generateField(SootField field) {
        if (RequiredFileGenerator.isRequired(field)) {
            StringBuffer fieldCode = new StringBuffer(_indent(1));
            fieldCode.append(CNames.typeNameOf(field.getType()) + " ");
            _updateRequiredTypes(field.getType());

            if (field.getType() instanceof ArrayType) {
                _context.addArrayInstance(CNames.typeNameOf(field.getType()));
            }

            fieldCode.append(CNames.fieldNameOf(field));
            fieldCode.append(";\n");

            _doneFields.add(field.getName());

            return fieldCode.toString();
        } else {
            return new String();
        }
    }

    // Generate C declarations corresponding to all non-static fields of
    // the class that we are presently generating code for. The public
    // and protected fields are declared first, followed by the private fields.
    private String _generateFields(SootClass source) {
        StringBuffer fieldCode = new StringBuffer();
        Iterator fields = source.getFields().iterator();
        int insertedFields = 0;
        String header = "\n" + _indent(1)
            + "/* Public and protected fields for " + source.getName()
            + " */\n";

        // Generate public and protected fields
        while (fields.hasNext()) {
            SootField field = (SootField) (fields.next());

            if ((!field.isPrivate())
                    && (!Modifier.isStatic(field.getModifiers()))
                    && (RequiredFileGenerator.isRequired(field))
                    && (!_doneFields.contains(field.getName()))) {
                if (insertedFields == 0) {
                    fieldCode.append(header);
                }

                fieldCode.append(_generateField(field));
                insertedFields++;
            }
        }

        // Generate private fields
        fieldCode.append("\n" + _indent(1) + "/* Private Fields */\n");
        fields = source.getFields().iterator();

        while (fields.hasNext()) {
            SootField field = (SootField) (fields.next());

            if (field.isPrivate() && !(Modifier.isStatic(field.getModifiers()))
                    && (!_doneFields.contains(field.getName()))) {
                if (insertedFields == 0) {
                    fieldCode.append(header);
                }

                fieldCode.append(_generateField(field));
                insertedFields++;
            }
        }

        return fieldCode.toString();
    }

    /** Generate C declarations corresponding to all non-static fields inherited
     *  from a given superclass.
     *  Rules for visibility:
     *  Public    fields  - globally visible.
     *  Protected fields  - visible only to subclasses
     *  Private   fields  - visible only inside the class.
     *  "friendly" fields - visible in the same package.
     */
    private String _generateInheritedFields(SootClass source,
            SootClass superClass) {
        StringBuffer fieldCode = new StringBuffer();
        Iterator fields = superClass.getFields().iterator();
        int insertedFields = 0;
        String header = "\n" + _indent(1)
            + _comment("Fields inherited from " + superClass.getName());

        while (fields.hasNext()) {
            SootField field = (SootField) (fields.next());

            boolean stat = Modifier.isStatic(field.getModifiers());
            boolean priv = field.isPrivate();
            boolean pub = field.isPublic();
            boolean prot = field.isProtected();
            boolean friendly = (!priv) && (!pub) && (!prot);
            boolean samePack = (source.getPackageName().compareTo(superClass
                                        .getPackageName()) == 0);

            // Whether this field should be visible to this class.
            boolean visible = (!stat)
                && (pub || prot || (friendly && samePack));

            // If a field has already been inherited from another class, it
            // need not be declared again.
            if (visible && (!_doneFields.contains(field.getName()))) {
                if (insertedFields == 0) {
                    fieldCode.append(header);
                }

                fieldCode.append(_generateField(field));
                insertedFields++;
            }
        }

        return fieldCode.toString();
    }

    // Return the superclasses of a class as a linked list.
    // The list entries are ordered in decreasing (parents before children)
    // hierarchy order.
    private LinkedList _getSuperClasses(SootClass source) {
        LinkedList classes = (LinkedList) (_superClasses.get(source));

        if (classes == null) {
            if (source.hasSuperclass()) {
                classes = (LinkedList) (_getSuperClasses(source.getSuperclass())
                        .clone());
                classes.add(source.getSuperclass());
                _superClasses.put(source, classes);
            } else {
                _superClasses.put(source, classes = new LinkedList());
            }
        }

        return classes;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////
    private Context _context;

    // Mapping from classes into lists of superclasses as computed by
    // {@link #_getSuperClasses(SootClass)}.
    private static HashMap _superClasses = new HashMap();

    // Prevents declaration of fields already declared.
    private static HashSet _doneFields;
}
