/* Javadoc Doclet that generates PtDoc XML

 Copyright (c) 2006-2013 The Regents of the University of California.
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

import ptolemy.util.StringUtilities;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.Tag;

/** Generate PtDoc output.
 *  See ptolemy/vergil/basic/DocML_1.dtd for the dtd.
 *
 *  <p>If javadoc is called with -d <i>directoryName</i>, then
 *  documentation will be generated in <i>directoryName</i>.
 *  If the KEPLER property is set, then for a class named
 *  <code>foo.bar.Baz</code>, the generated file is named
 *  <code>Baz.doc.xml</code>.  If the KEPLER property is not
 *  set, then the generated file is named <code>foo/bar/Baz.xml</code>.
 *
 *  <p>This doclet writes the names of all the classes for which
 *  documentation was generated in a file called allNamedObjs.txt
 *
 *  @author Christopher Brooks, Edward A. Lee, Contributors: Nandita Mangal, Ian Brown
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
     *  that extend ptolemy.actor.TypedAtomicActor are processed, all
     *  other classes are ignored.
     *  @param root The root of the java doc tree.
     *  @return Always return true;
     *  @exception IOException If there is a problem writing the documentation.
     *  @exception ClassNotFoundException If there is a problem finding
     *  the class of one of the fields.
     */
    public static boolean start(RootDoc root) throws IOException,
            ClassNotFoundException {
        System.out.println("Ptolemy version of PtDoc, with Kepler extensions");
        if (!StringUtilities.getProperty("KEPLER").equals("")) {
            System.out.println("PtDoclet: KEPLER = "
                    + StringUtilities.getProperty("KEPLER"));
        }

        // Used for keyword search of documentation
        _ptIndexer = new PtIndexer();

        _outputDirectory = _getOutputDirectory(root.options());
        // We cache the names of all actors for which we generate text.
        FileWriter allNamedObjsWriter = null;
        try {
            File outputDirectoryFile = new File(_outputDirectory);
            if (!outputDirectoryFile.isDirectory()) {
                if (!outputDirectoryFile.mkdirs()) {
                    throw new IOException("Failed to create \""
                            + _outputDirectory + "\"");
                }
            }
            allNamedObjsWriter = new FileWriter(_outputDirectory
                    + File.separator + "allNamedObjs.txt");

            ClassDoc namedObjDoc = root
                    .classNamed("ptolemy.kernel.util.NamedObj");

            Class typedIOPortClass = Class.forName("ptolemy.actor.TypedIOPort");
            Class parameterClass = Class.forName("ptolemy.data.expr.Parameter");
            // The expression in the Expression actor is a StringAttribute.
            Class stringAttributeClass = Class
                    .forName("ptolemy.kernel.util.StringAttribute");

            ClassDoc[] classes = root.classes();
            for (ClassDoc classe : classes) {
                String className = classe.toString();
                if (classe.subclassOf(namedObjDoc)) {
                    _writeDoc(
                            className,
                            _generateClassLevelDocumentation(classe)
                                    + _generateFieldDocumentation(classe,
                                            typedIOPortClass, "port")
                                    + _generateFieldDocumentation(classe,
                                            parameterClass, "property")
                                    + _generateFieldDocumentation(classe,
                                            stringAttributeClass, "property")
                                    + "</doc>\n");

                    allNamedObjsWriter.write(className + "\n");
                }
            }
        } finally {
            if (allNamedObjsWriter != null) {
                allNamedObjsWriter.close();
            }
        }
        // Running (cd $PTII/ptolemy/plot; make dists) comments
        // out lines with _ptIndexer in them.
        File ptIndexerSer = new File(_outputDirectory, "PtIndexer.ser");
        _ptIndexer.write(ptIndexerSer.getCanonicalPath());
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Process customTags and return text that contains links to the
     *  javadoc output.
     *  @param programElementDoc The class for which we are generating
     *  documentation.
     */
    private static String _customTagCommentText(
            ProgramElementDoc programElementDoc) {

        // Process the comment as an array of tags.  Doc.commentText()
        // should do this, but it does not.
        String documentation = "";

        Tag tag[] = programElementDoc.tags("UserLevelDocumentation");
        StringBuffer textTag = new StringBuffer();
        for (Tag element : tag) {
            textTag.append(element.text());
        }

        if (textTag.toString().length() > 0) {
            documentation = "<UserLevelDocumentation>"
                    + StringUtilities.escapeForXML(textTag.toString())
                    + "</UserLevelDocumentation>";
        }

        return documentation;
    }

    /** Process inlineTags and return text that contains links to the
     *  javadoc output.
     *  @param programElementDoc The class for which we are generating
     *  documentation.
     */
    private static String _inlineTagCommentText(
            ProgramElementDoc programElementDoc) {
        // Process the comment as an array of tags.  Doc.commentText()
        // should do this, but it does not.
        StringBuffer documentation = new StringBuffer();
        Tag tag[] = programElementDoc.inlineTags();
        for (Tag element : tag) {
            if (element instanceof SeeTag) {
                SeeTag seeTag = (SeeTag) element;
                documentation.append("<a href=\"");
                // The dot separated class or package name, if any.
                String classOrPackageName = null;
                boolean isIncluded = false;
                if (seeTag.referencedPackage() != null) {
                    classOrPackageName = seeTag.referencedPackage().toString();
                    isIncluded = seeTag.referencedPackage().isIncluded();
                }
                if (seeTag.referencedClass() != null) {
                    classOrPackageName = seeTag.referencedClass()
                            .qualifiedName();
                    isIncluded = seeTag.referencedClass().isIncluded();
                }

                // {@link ...} tags usually have a null label.
                String target = seeTag.label();
                if (target == null || target.length() == 0) {
                    target = seeTag.referencedMemberName();
                    if (target == null || target.length() == 0) {
                        target = seeTag.referencedClassName();
                    }
                }
                if (classOrPackageName != null) {
                    if (target != null && target.indexOf("(") != -1) {
                        // The target has a paren, so can't be a port or
                        // parameter, so link to the html instead of the .xml.

                        isIncluded = false;
                    }

                    // If the .xml file is not included in the output,
                    // then link to the .html file
                    documentation.append(_relativizePath(_outputDirectory,
                            classOrPackageName, programElementDoc, isIncluded));
                }
                if (seeTag.referencedMember() != null) {
                    documentation
                            .append("#" + seeTag.referencedMember().name());
                }
                documentation.append("\">" + target + "</a>");
            } else {
                documentation.append(element.text());
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
            shortClassName = className
                    .substring(className.lastIndexOf(".") + 1);
        }

        StringBuffer documentation = new StringBuffer(_header + "<doc name=\""
                + shortClassName + "\" class=\"" + className + "\">\n"
                + "  <description>\n"
                + StringUtilities.escapeForXML(_inlineTagCommentText(classDoc))
                + "  </description>\n");

        Tag[] tags = null;
        // Handle other class tags.
        String[] classTags = { "author", "version", "since",
                "Pt.ProposedRating", "Pt.AcceptedRating",
                "UserLevelDocumentation" };
        for (String classTag : classTags) {
            tags = classDoc.tags(classTag);
            if (tags.length > 0) {
                StringBuffer textTag = new StringBuffer();
                for (Tag tag : tags) {
                    textTag.append(tag.text());
                }
                documentation.append("  <" + classTag + ">"
                        + StringUtilities.escapeForXML(textTag.toString())
                        + "</" + classTag + ">\n");
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
            Class fieldBaseClass, String element) throws ClassNotFoundException {
        StringBuffer documentation = new StringBuffer();
        FieldDoc[] fields = classDoc.fields();
        // FIXME: get fields from superclasses?
        for (FieldDoc field : fields) {
            String className = field.type().toString();
            //System.out.println(element + ": Processing " + className);

            try {
                if (className.equals("javax.media.j3d.Canvas3D")
                        || className
                                .equals("com.sun.j3d.utils.universe.SimpleUniverse")) {
                    throw new Exception("Skipping " + className
                            + ",it starts up X11 and interferes with the "
                            + "nightly build");
                }
                Class type = Class.forName(className);
                if (fieldBaseClass.isAssignableFrom(type)) {
                    documentation.append("    <!--"
                            + className
                            + "-->\n"
                            + "    <"
                            + element
                            + " name=\""
                            + field.name()
                            + "\">"
                            + StringUtilities
                                    .escapeForXML(_inlineTagCommentText(field))
                            + _customTagCommentText(field) + "</" + element
                            + ">\n");
                }
            } catch (ClassNotFoundException ex) {
                // Ignored, we probably have a primitive type like boolean.
                // Java 1.5 Type.isPrimitive() would help here.
            } catch (Throwable throwable) {
                // Ignore, probably a loader error for Java3D
                System.out.println("Failed to finde class " + className);
                throwable.printStackTrace();
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
    private static String _getOutputDirectory(String[][] options) {
        for (String[] option : options) {
            if (option[0].equals("-d")) {
                return option[1];
            }
        }
        return null;
    }

    /** Given two dot separated classpath names, return a relative
     *  path to the corresponding doc file.
     *  This method is used to create relative paths
     *  @param baseDirectory The top level directory where the classes are written.
     *  @param destinationClassName The dot separated fully qualified class name.
     *  @param programElementDoc The documentation for the base class.
     *  @param isIncluded True if the destination class is included in the
     *  set of classes we are documenting.  If isIncluded is true,
     *  we create a link to the .xml file.  If isIncluded is false, we
     *  create a link to the javadoc .html file.
     *  @return a relative path from the base class to the destination class.
     */
    private static String _relativizePath(String baseDirectory,
            String destinationClassName, ProgramElementDoc programElementDoc,
            boolean isIncluded) {
        // Use / here because these will be used in URLS
        //String baseFileName = baseClassName.replace('.', "/");
        String baseClassName = programElementDoc.qualifiedName();
        String destinationFileName = destinationClassName.replace('.', '/');
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
        String extension = isIncluded ? ".xml" : ".html";

        System.out.println("PtDoclet: relativize: " + baseDirectory + " "
                + baseClassName + " " + baseClassParts.length + " " + offset
                + " " + relativePath + relativeURI.getPath() + extension);

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
        String fileBaseName = className.replace('.', File.separatorChar)
                + ".xml";

        _ptIndexer.append(className, documentation);

        if (!StringUtilities.getProperty("KEPLER").equals("")) {
            // If we are running in Kepler, the put the output somewhere else.
            fileBaseName = className.substring(className.lastIndexOf('.') + 1)
                    + ".doc.xml";
        }

        String fileName = null;
        if (_outputDirectory != null) {
            fileName = _outputDirectory + File.separator + fileBaseName;
        } else {
            fileName = fileBaseName;
        }
        // If necessary, create the directory.
        File directoryFile = new File(fileName).getParentFile();
        if (!directoryFile.exists()) {
            if (!directoryFile.mkdirs()) {
                throw new IOException("Directory \"" + directoryFile
                        + "\" does not exist and cannot be created.");
            }
        }
        System.out.println("Creating " + fileName);

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

    /** Directory to which the output is to be written. */
    private static String _outputDirectory;

    /** Index of keywords in the documentation. */
    private static PtIndexer _ptIndexer;
}
