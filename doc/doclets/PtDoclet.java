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
import java.net.URI;
import com.sun.javadoc.*;

import ptolemy.util.StringUtilities;

/** Generate PtDoc output.
 *  See ptolemy/vergil/basic/DocML_1.dtd for the dtd. 
 *  <p>If javadoc is called with -d <i>directoryName</i>, then 
 *  documentation will be generated in <i>directoryName</i>.
 *  This doclet writes the names of all the classes for which documentation
 *  was generated in a file called allActors.txt.

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

    /** Process the java files and generate PtDoc XML.  Only classes
     *  that extend ptolemy.kernel.util.NamedObj are processed, all
     *  other classes are ignored.
     *  @param root The root of the java doc tree.
     *  @return Always return true;
     *  @exception IOException If there is a problem writing the documentation.
     *  @exception ClassNotFoundException If there is a problem finding
     *  the class of one of the fields.
     */
    public static boolean start(RootDoc root)
            throws IOException, ClassNotFoundException {
        _outputDirectory = _getOutputDirectory(root.options());
        // We cache the names of all actors for which we generate text.
        FileWriter allActorsWriter = null;
        try {
            allActorsWriter = new FileWriter(
                    _outputDirectory + File.separator + "allActors.txt");

            Class typedIOPortClass = Class.forName("ptolemy.actor.TypedIOPort");
            Class parameterClass = Class.forName("ptolemy.data.expr.Parameter");

            ClassDoc[] classes = root.classes();
            for (int i = 0; i < classes.length; i++) {
                String className = classes[i].toString();
                //System.out.println(className);
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
                if (!_namedObjClass.isAssignableFrom(theClass)) {
                    // The class does not extend TypedAtomicActor, so we skip.
                    continue;
                }


                StringBuffer documentation =
                    _generateClassLevelDocumentation(classes[i]);
                documentation.append(_generateFieldDocumentation(classes[i], 
                                             typedIOPortClass, "port"));
                documentation.append(_generateFieldDocumentation(classes[i], 
                                             parameterClass, "property"));
                documentation.append("</doc>\n");
                _writeDoc(className, documentation.toString());
                allActorsWriter.write(className + "\n");
            }
        } finally {
            if (allActorsWriter != null) {
                allActorsWriter.close();
            }
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    
    /** Process inlineTags and return text that contains links to the
     *  javadoc output.
     *  @param programElementDoc The class for which we are generating
     *  documentation.
     */
    private static String _inlineTagCommentText(ProgramElementDoc
            programElementDoc) {
        // Process the comment as an array of tags.  Doc.commentText()
        // should do this, but it does not.
        StringBuffer documentation = new StringBuffer();
        Tag tag[] = programElementDoc.inlineTags();
        for (int i = 0; i < tag.length; i++) {
            if (tag[i] instanceof SeeTag) {
                SeeTag seeTag = (SeeTag)tag[i]; 
                documentation.append("<a href=\"");
                // The dot separated class or package name, if any.
                String classOrPackageName = null;
                boolean isIncluded = false;
                if (seeTag.referencedPackage() != null) {
                    classOrPackageName = 
                        seeTag.referencedPackage().toString();
                    isIncluded = seeTag.referencedPackage().isIncluded();
                } 
                if (seeTag.referencedClass() != null) {
                    classOrPackageName = 
                        seeTag.referencedClass().qualifiedName();
                    isIncluded = seeTag.referencedClass().isIncluded();
                }
                if (classOrPackageName != null) {
                    // FIXME: If the .xml file does not exist,
                    // then link to the .htm file
                    documentation.append(
                            _relativizePath(_outputDirectory,
                                    classOrPackageName, programElementDoc,
                                    isIncluded));
                }
                if (seeTag.referencedMember() != null) {
                    documentation.append("#" +
                            seeTag.referencedMember().name());
                }
                String target = seeTag.label();
                if (target.length() == 0) {
                    target = seeTag.referencedClassName() + "#"  
                        + seeTag.referencedMemberName();
                }
                documentation.append("\">" + target + "</a>");
            } else {
                documentation.append(tag[i].text());
            }
        }
        return documentation.toString();
    }

    /** Generate the classLevel documentation for a class
     *  @param classDoc The class for which we are generating documentation.
     */
    private static StringBuffer _generateClassLevelDocumentation(
            ClassDoc classDoc) {
        // This method is a private method so that the start() method
        // is easier to read.
        String className = classDoc.toString();
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
                + StringUtilities.escapeForXML(
                        _inlineTagCommentText(classDoc))
                + "  </description>\n");

        // Handle other class tags.
        String [] classTags = {"author", "version", "since",
                               "Pt.ProposedRating", "Pt.AcceptedRating"};
        for (int j = 0; j< classTags.length; j++) {
            Tag [] tags = classDoc.tags(classTags[j]);
            // FIXME: This uses just the first tag.
            if (tags.length > 0) {
                documentation.append("  <" + classTags[j] + ">" 
                        + StringUtilities.escapeForXML(tags[0].text())
                        + "</" + classTags[j] + ">\n");
            }
        }
        return documentation;
    }

    /** Generate documentation for all fields that are derived from a
     *  specific base class.  The class inheritance tree is traversed
     *  up to and including NamedObj and then the traversal stops.
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
            //System.out.println(element + ": Processing " + className);

            try {
                Class type = Class.forName(className);
                if (fieldBaseClass.isAssignableFrom(type)) {
                    documentation.append(
                            "    <!--" + className + "-->\n"
                            + "    <" + element + " name=\""
                            + fields[i].name() + "\">" 
                            + StringUtilities.escapeForXML(
                                    _inlineTagCommentText(fields[i]))
                            + "</" + element + ">\n");
                }
             } catch (ClassNotFoundException ex) {
                 // Ignored, we probably have a primitive type like boolean.
                 // Java 1.5 Type.isPrimitive() would help here.
             }
        }

//         // Go up the hierarchy
//         ClassDoc superClassDoc = classDoc.superclass(); 
//         if (superClassDoc != null) {
//             //System.out.println(element + ": SuperClass " + superClassDoc);

//             try {
//                 Class superClass = Class.forName(superClassDoc.toString()); 
//                 // Go no higher than TypedAtomicActor
//                 if (_namedObjClass.isAssignableFrom(superClass)) {
//                     documentation.append(_generateFieldDocumentation(
//                                                  superClassDoc,
//                                                  fieldBaseClass, element));
//                 } 
//             } catch (Throwable throwable) {
//                 System.err.println("Failed to find superclass "
//                         + superClassDoc + "\n" + throwable);
//             }
//         }
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
    
    /** Given two dot separated classpath names, return a relative
     *  path to the corresponding doc file.
     *  This method is used to create relative paths
     *  @param directory The top level directory where the classes are written.
     *  @param destinationClassName
     *  @param programElementDoc The documentation for the base class.
     *  @param isIncluded True if the destination class is included in the
     *  set of classes we are documenting.  If isIncluded is true,
     *  we create a link to the .xml file.  If isIncluded is false, we
     *  create a linke to the javadoc .html file.
     *  @return a relative path from the base class to the destination class.
     */
    private static String _relativizePath(String baseDirectory,
            String destinationClassName, ProgramElementDoc programElementDoc,
            boolean isIncluded) {
        // Use / here because these will be used in URLS
        //String baseFileName = baseClassName.replace('.', "/");
        String baseClassName = programElementDoc.qualifiedName();
        String destinationFileName =
            destinationClassName.replace('.', '/');
        if (baseDirectory != null) {
            // FIXME: will this work if baseDirectory is null?
            //baseFileName = baseDirectory + "/" + baseFileName;
            destinationFileName = baseDirectory + "/" + destinationFileName;
        }
        //URI baseURI = new File(baseFileName).toURI();
        URI destinationURI = new File(destinationFileName).toURI();
        URI baseDirectoryURI = new File(baseDirectory).toURI();
        URI relativeURI = baseDirectoryURI.relativize(destinationURI);

        // Determine offsite from baseClassName to baseDirectory
        String baseClassParts[] = baseClassName.split("\\.");
        StringBuffer relativePath = new StringBuffer();

        int offset = 1;
        if (programElementDoc instanceof FieldDoc) {
            // Fields have names like foo.bar.bif, where bif is the method
            offset = 2;
        }
        for (int i = 0; i < baseClassParts.length - offset; i++) {
            relativePath.append("../");
        }

        
        // If the target is not in the list of actors we are creating
        // documentation for, then link to the .html file that
        // presumably was generated by javadoc; otherwise, link to the
        // .xml file
        String extension = (isIncluded ? ".xml" : ".html");

        System.out.println("PtDoclet: relativize: "
                + baseDirectory + " " + baseClassName + " "
                + baseClassParts.length + " " + offset + " "
                + relativePath + relativeURI.getPath() + extension);
                

        return relativePath + relativeURI.getPath() + extension;
    }


    /** Write the output to a file.  
     *  @param className The dot separated fully qualified classname,
     *  which is used to specify the directory and filename to which
     *  the documentation is written.
     *  @param documentation The documentation that is written.
     *  @exception IOException If there is a problem writing the documentation.
     */
    private static void _writeDoc(String className, String documentation)
        throws IOException {
        String fileBaseName = className.replace('.', File.separatorChar) + ".xml";
        String fileName = null;
        if (_outputDirectory != null) {
            fileName = _outputDirectory + File.separator + fileBaseName;
        } else {
            fileName = fileBaseName;
        }
        // If necessary, create the directory.
        File directoryFile = new File(fileName).getParentFile();
        if (!directoryFile.exists()) {
            directoryFile.mkdirs();
        }
        System.out.println("Creating "
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

    /** Directory to which the output is to be written. */
    private static String _outputDirectory;

    /** Header string for XML PtDoc output. */
    private static String _header = "<?xml version=\"1.0\" standalone=\"yes\"?>\n<!DOCTYPE doc PUBLIC \"-//UC Berkeley//DTD DocML 1//EN\"\n    \"http://ptolemy.eecs.berkeley.edu/xml/dtd/DocML_1.dtd\">\n";

    private static Class _namedObjClass;
    static {
        try {
            _namedObjClass = Class.forName("ptolemy.kernel.util.NamedObj");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }
}
