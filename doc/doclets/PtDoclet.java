/* Javadoc Doclet that generates PtDoc XML

 Copyright (c) 2006 The Regents of the University of California.
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

package doc.doclets;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import com.sun.javadoc.*;

import ptolemy.util.StringUtilities;

/** Generate PtDoc output.
 *  @author Christopher Brooks, Edward A. Lee
 *  @version $Id$
 *  @since Ptolemy II 5.2  
 */
public class PtDoclet {
    /** Given a command line option, return the number of command line
     *  arguments needed by that option.
     *  @param option The command line option
     *  @return If the option is "-d", return 2; otherwise, return 0.
     */
    public static int optionLength(String option) {
        if (option.equals("-d")) {
	    return 2;
        }
        return 0;
    }

    /** Process the java files and generate PtDoc XML.
     *  Only classes that extend TypedAtomicActor are processed, all
     *  other classes are ignored.
     *  @param root The root of the java doc tree.
     *  @return Always return true;
     *  @exception IOException If there is a problem writing the documentation.
     *  @exception ClassNotFoundException If there is a problem finding
     *  the class of one of the fields.
     */
    public static boolean start(RootDoc root)
            throws IOException, ClassNotFoundException {
        String outputDirectory = _getOutputDirectory(root.options());
        Class typedIOPortClass = Class.forName("ptolemy.actor.TypedIOPort");
        Class parameterClass = Class.forName("ptolemy.data.expr.Parameter");

        ClassDoc[] classes = root.classes();
        for (int i = 0; i < classes.length; i++) {
            String className = classes[i].toString();

            Class theClass = null;
            try {
                theClass = Class.forName(className);
            } catch (Throwable ex) {
                // Print a message and move on.
                // FIXME: Use the doclet error handling mechanism
                System.err.println("Failed to process " + className + "\n"
                        + ex);
                continue;
            }
            if (!_typedAtomicActorClass.isAssignableFrom(theClass)) {
                // The class does not extend TypedAtomicActor, so we skip.
                continue;
            }

            String shortClassName = null;
            if (className.lastIndexOf(".") == -1) {
                shortClassName = className;
            } else {
                shortClassName = 
                    className.substring(className.lastIndexOf(".") + 1);
            }

            StringBuffer documentation = new StringBuffer(_header
                    + "<doc name=\"" + shortClassName
                    + "\" class=\"" + className + "\">\n"
                    + "  <description>\n"
                    + StringUtilities.escapeForXML(classes[i].commentText())
                    + "\n"
                    + "  </description>\n");

            // Handle other class tags.
            String [] classTags = {"author", "version", "since",
                                   "Pt.ProposedRating", "Pt.AcceptedRating"};
            for (int j = 0; j< classTags.length; j++) {
                Tag [] tags = classes[i].tags(classTags[j]);
                // FIXME: This uses just the first tag.
                if (tags.length > 0) {
                    documentation.append("  <" + classTags[j] + ">" 
                            + StringUtilities.escapeForXML(tags[0].text())
                            + "</" + classTags[j] + ">\n");
                }
            }

            documentation.append(_generateFieldDocumentation(classes[i], 
                                         typedIOPortClass, "port"));

            documentation.append(_generateFieldDocumentation(classes[i], 
                                         parameterClass, "parameter"));
            documentation.append("</doc>\n");
            _writeDoc(className, outputDirectory, documentation.toString());
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Generate documentation for all fields that are derived
     *  from a specific base class.  The class inheritance tree is
     *  traversed up to and including TypedAtomicActor and then
     *  the traversal stops.
     *  @param classDoc The ClassDoc for the class we are documenting.
     *  @param fieldBaseClass The base class for the field we are documenting.
     *  @param element The XML element that is generated.
     *  @return The documentation for all fields that are derived from 
     *  the fieldBaseClass parameter.
     *  @exception ClassNotFoundException If the class of a field cannot
     *  be found.
     */
    private static String _generateFieldDocumentation(ClassDoc classDoc,
            Class fieldBaseClass, String element)
            throws ClassNotFoundException {
        StringBuffer documentation = new StringBuffer();
        FieldDoc[] fields = classDoc.fields();
        // FIXME: get fields from superclasses?
        for (int i = 0; i < fields.length; i++) {
            String className = fields[i].type().toString();
            try {
                Class type = Class.forName(className);
                if (fieldBaseClass.isAssignableFrom(type)) {
                    documentation.append(
                            "    <!--" + className + "-->\n"
                            + "    <" + element + " name=\""
                            + fields[i].name() + "\">" 
                            + StringUtilities.escapeForXML(fields[i].commentText())
                            + "</" + element + ">\n");
                }
             } catch (ClassNotFoundException ex) {
                 // Ignored, we probably have a primitive type like boolean.
                 // Java 1.5 Type.isPrimitive() would help here.
             }
        }
        // Go up the hierarchy
        ClassDoc superClassDoc = classDoc.superclass(); 
        if (superClassDoc != null) {
            Class superClass = Class.forName(superClassDoc.toString()); 
            // Go no higher than TypedAtomicActor
            if (_typedAtomicActorClass.isAssignableFrom(superClass)) {
                System.out.println(element + ": Processing " + superClassDoc);
                documentation.append(_generateFieldDocumentation(
                                             superClassDoc,
                                             fieldBaseClass, element));
            }
        }
        return documentation.toString();
    }

    /** Process the doclet command line arguments and return the value
     *  of the -d parameter, if any.
     *  @param options The command line options.
     *  @return the value of the -d parameter, if any, otherwise return null.
     */
    private static String _getOutputDirectory(String [][] options) {
        for (int i = 0; i < options.length; i++) {
            String[] option = options[i];
            if (option[0].equals("-d")) {
                return option[1];
            }
        }
        return null;
    }
    
    /** Write the output to a file.  
     *  @param className The dot separated fully qualified classname,
     *  which is used to specify the directory and filename to which
     *  the documentation is written.
     *  @param directory The top level directory where the classes are written.
     *  If necessary, the directory is created.
     *  @param documentation The documentation that is written.
     *  @exception IOException If there is a problem writing the documentation.
     */
    private static void _writeDoc(String className, String directory,
            String documentation) throws IOException {
        String fileBaseName = className.replace('.', File.separatorChar) + ".xml";
        String fileName = null;
        if (directory != null) {
            fileName = directory + File.separator + fileBaseName;
        } else {
            fileName = fileBaseName;
        }
        // If necessary, create the directory.
        File directoryFile = new File(fileName).getParentFile();
        if (!directoryFile.exists()) {
            directoryFile.mkdirs();
        }
        System.out.println("Writing " + documentation.length() + " chars to "
                + System.getProperty("user.dir") + File.separator + fileName);

        FileWriter writer = new FileWriter(fileName);
        try {
            writer.write(documentation);
        } finally {
            writer.close();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Header string for XML PtDoc output. */
    private static String _header = "<?xml version=\"1.0\" standalone=\"yes\"?>\n<!DOCTYPE doc PUBLIC \"-//UC Berkeley//DTD DocML 1//EN\"\n    \"http://ptolemy.eecs.berkeley.edu/xml/dtd/DocML_1.dtd\">\n";

    private static Class _typedAtomicActorClass;
    static {
        try {
            _typedAtomicActorClass = Class.forName("ptolemy.actor.TypedAtomicActor");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }



}
