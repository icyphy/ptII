/* Javadoc Doclet that generates PtDoc XML

 Copyright (c) 2006-2026 The Regents of the University of California.
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
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.LinkTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.doctree.UnknownBlockTagTree;
import com.sun.source.util.DocTrees;

import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

import ptolemy.util.StringUtilities;

/** Generate PtDoc output.
 *  See ptolemy/vergil/basic/DocML_1.dtd for the dtd.
 *
 *  <p>This is the JDK 9+ port of the original PtDoclet, which used the
 *  removed com.sun.javadoc API. It now implements the jdk.javadoc.doclet.Doclet
 *  interface and uses javax.lang.model and com.sun.source.doctree APIs.
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
public class PtDoclet implements Doclet {

    @Override
    public void init(Locale locale, Reporter reporter) {
        _reporter = reporter;
    }

    @Override
    public String getName() {
        return "PtDoclet";
    }

    @Override
    public Set<? extends Option> getSupportedOptions() {
        return Set.of(new Option() {
            @Override
            public int getArgumentCount() {
                return 1;
            }

            @Override
            public String getDescription() {
                return "Output directory";
            }

            @Override
            public Kind getKind() {
                return Kind.STANDARD;
            }

            @Override
            public List<String> getNames() {
                return List.of("-d");
            }

            @Override
            public String getParameters() {
                return "directory";
            }

            @Override
            public boolean process(String option, List<String> arguments) {
                _outputDirectory = arguments.get(0);
                return true;
            }
        });
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean run(DocletEnvironment environment) {
        System.out.println("Ptolemy version of PtDoc, with Kepler extensions");
        if (!StringUtilities.getProperty("KEPLER").equals("")) {
            System.out.println("PtDoclet: KEPLER = "
                    + StringUtilities.getProperty("KEPLER"));
        }

        _docTrees = environment.getDocTrees();
        _elementUtils = environment.getElementUtils();
        _typeUtils = environment.getTypeUtils();
        _environment = environment;
        _ptIndexer = new PtIndexer();

        Class typedIOPortClass;
        Class parameterClass;
        Class stringAttributeClass;
        try {
            typedIOPortClass = Class.forName("ptolemy.actor.TypedIOPort");
            parameterClass = Class.forName("ptolemy.data.expr.Parameter");
            stringAttributeClass = Class
                    .forName("ptolemy.kernel.util.StringAttribute");
        } catch (ClassNotFoundException ex) {
            _reporter.print(Diagnostic.Kind.ERROR,
                    "Failed to load Ptolemy classes: " + ex.getMessage());
            return false;
        }

        TypeElement namedObjElement = _elementUtils
                .getTypeElement("ptolemy.kernel.util.NamedObj");
        if (namedObjElement == null) {
            _reporter.print(Diagnostic.Kind.ERROR,
                    "Could not find ptolemy.kernel.util.NamedObj on the classpath.");
            return false;
        }
        TypeMirror namedObjType = namedObjElement.asType();

        FileWriter allNamedObjsWriter = null;
        try {
            if (_outputDirectory == null) {
                throw new IOException(
                        "No output directory specified. Use -d directory.");
            }
            File outputDirectoryFile = new File(_outputDirectory);
            if (!outputDirectoryFile.isDirectory()) {
                if (!outputDirectoryFile.mkdirs()) {
                    throw new IOException(
                            "Failed to create \"" + _outputDirectory + "\"");
                }
            }
            allNamedObjsWriter = new FileWriter(
                    _outputDirectory + File.separator + "allNamedObjs.txt");

            Set<? extends Element> specifiedElements = environment
                    .getSpecifiedElements();

            for (Element element : specifiedElements) {
                _processElement(element, namedObjType, typedIOPortClass,
                        parameterClass, stringAttributeClass,
                        allNamedObjsWriter);
            }
        } catch (IOException ex) {
            _reporter.print(Diagnostic.Kind.ERROR, ex.getMessage());
            return false;
        } finally {
            if (allNamedObjsWriter != null) {
                try {
                    allNamedObjsWriter.close();
                } catch (IOException ex) {
                    // Ignore.
                }
            }
        }

        try {
            File ptIndexerSer = new File(_outputDirectory, "PtIndexer.ser");
            _ptIndexer.write(ptIndexerSer.getCanonicalPath());
        } catch (IOException ex) {
            _reporter.print(Diagnostic.Kind.ERROR,
                    "Failed to write PtIndexer: " + ex.getMessage());
            return false;
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Recursively process an element, looking for TypeElements (classes)
     *  that are subclasses of NamedObj.
     */
    private void _processElement(Element element, TypeMirror namedObjType,
            Class typedIOPortClass, Class parameterClass,
            Class stringAttributeClass, FileWriter allNamedObjsWriter)
            throws IOException {
        if (element.getKind() == ElementKind.PACKAGE) {
            for (Element enclosed : element.getEnclosedElements()) {
                _processElement(enclosed, namedObjType, typedIOPortClass,
                        parameterClass, stringAttributeClass,
                        allNamedObjsWriter);
            }
            return;
        }
        if (element instanceof TypeElement) {
            TypeElement typeElement = (TypeElement) element;
            String className = typeElement.getQualifiedName().toString();

            try {
                if (_typeUtils.isSubtype(typeElement.asType(),
                        _typeUtils.erasure(namedObjType))) {
                    _writeDoc(className,
                            _generateClassLevelDocumentation(typeElement)
                                    + _generateFieldDocumentation(typeElement,
                                            typedIOPortClass, "port")
                                    + _generateFieldDocumentation(typeElement,
                                            parameterClass, "property")
                                    + _generateFieldDocumentation(typeElement,
                                            stringAttributeClass, "property")
                                    + "</doc>\n");

                    allNamedObjsWriter.write(className + "\n");
                }
            } catch (Throwable throwable) {
                System.out.println(
                        "PtDoclet: Error processing " + className + ": "
                                + throwable.getMessage());
            }

            for (Element enclosed : typeElement.getEnclosedElements()) {
                if (enclosed.getKind() == ElementKind.CLASS
                        || enclosed.getKind() == ElementKind.INTERFACE) {
                    _processElement(enclosed, namedObjType, typedIOPortClass,
                            parameterClass, stringAttributeClass,
                            allNamedObjsWriter);
                }
            }
        }
    }

    /** Extract the text content of a list of DocTree nodes, handling
     *  inline {@link} tags by producing HTML anchor elements.
     */
    private String _inlineTagCommentText(Element element) {
        DocCommentTree docCommentTree = _docTrees.getDocCommentTree(element);
        if (docCommentTree == null) {
            return "";
        }
        StringBuilder documentation = new StringBuilder();
        for (DocTree docTree : docCommentTree.getFullBody()) {
            _appendDocTree(documentation, docTree, element);
        }
        return documentation.toString();
    }

    /** Append the text representation of a single DocTree node. */
    private void _appendDocTree(StringBuilder sb, DocTree docTree,
            Element contextElement) {
        switch (docTree.getKind()) {
        case TEXT:
            sb.append(((TextTree) docTree).getBody());
            break;
        case LINK:
        case LINK_PLAIN:
            LinkTree linkTree = (LinkTree) docTree;
            ReferenceTree ref = linkTree.getReference();
            String refSig = ref.getSignature();
            List<? extends DocTree> label = linkTree.getLabel();

            String labelText = "";
            if (label != null && !label.isEmpty()) {
                StringBuilder labelSb = new StringBuilder();
                for (DocTree lt : label) {
                    labelSb.append(lt.toString());
                }
                labelText = labelSb.toString().trim();
            }
            if (labelText.isEmpty()) {
                labelText = refSig;
            }

            String qualifiedRef = _resolveReference(refSig, contextElement);
            if (qualifiedRef != null) {
                String contextQualifiedName = _getQualifiedName(contextElement);
                boolean isIncluded = false;
                TypeElement refElement = _elementUtils
                        .getTypeElement(qualifiedRef);
                if (refElement != null) {
                    isIncluded = _environment.isIncluded(refElement);
                }
                if (labelText.indexOf("(") != -1) {
                    isIncluded = false;
                }
                sb.append("<a href=\"");
                sb.append(_relativizePath(_outputDirectory, qualifiedRef,
                        contextQualifiedName, false, isIncluded));
                sb.append("\">");
                sb.append(labelText);
                sb.append("</a>");
            } else {
                sb.append(labelText);
            }
            break;
        default:
            sb.append(docTree.toString());
            break;
        }
    }

    /** Try to resolve a reference string to a fully qualified class name. */
    private String _resolveReference(String refSig, Element contextElement) {
        if (refSig == null || refSig.isEmpty()) {
            return null;
        }
        String classRef = refSig;
        int hashIndex = classRef.indexOf('#');
        if (hashIndex >= 0) {
            classRef = classRef.substring(0, hashIndex);
        }
        int parenIndex = classRef.indexOf('(');
        if (parenIndex >= 0) {
            classRef = classRef.substring(0, parenIndex);
        }
        classRef = classRef.trim();
        if (classRef.isEmpty()) {
            return _getQualifiedName(contextElement);
        }
        TypeElement resolved = _elementUtils.getTypeElement(classRef);
        if (resolved != null) {
            return resolved.getQualifiedName().toString();
        }
        Element enclosing = contextElement;
        while (enclosing != null && !(enclosing instanceof TypeElement)) {
            enclosing = enclosing.getEnclosingElement();
        }
        if (enclosing instanceof TypeElement) {
            String pkg = _elementUtils
                    .getPackageOf(enclosing).getQualifiedName().toString();
            if (!pkg.isEmpty()) {
                resolved = _elementUtils.getTypeElement(pkg + "." + classRef);
                if (resolved != null) {
                    return resolved.getQualifiedName().toString();
                }
            }
        }
        return classRef;
    }

    /** Get the qualified name for an element. For fields, returns the
     *  enclosing class's qualified name + "." + field name.
     */
    private String _getQualifiedName(Element element) {
        if (element instanceof TypeElement) {
            return ((TypeElement) element).getQualifiedName().toString();
        }
        if (element instanceof VariableElement) {
            Element enclosing = element.getEnclosingElement();
            if (enclosing instanceof TypeElement) {
                return ((TypeElement) enclosing).getQualifiedName().toString()
                        + "." + element.getSimpleName().toString();
            }
        }
        return element.toString();
    }

    /** Get text for a custom block tag (e.g. @UserLevelDocumentation). */
    private String _customTagCommentText(Element element) {
        DocCommentTree docCommentTree = _docTrees.getDocCommentTree(element);
        if (docCommentTree == null) {
            return "";
        }
        StringBuilder textTag = new StringBuilder();
        for (DocTree docTree : docCommentTree.getBlockTags()) {
            if (docTree.getKind() == DocTree.Kind.UNKNOWN_BLOCK_TAG) {
                UnknownBlockTagTree unknownTag = (UnknownBlockTagTree) docTree;
                if ("UserLevelDocumentation"
                        .equals(unknownTag.getTagName())) {
                    for (DocTree content : unknownTag.getContent()) {
                        textTag.append(content.toString());
                    }
                }
            }
        }
        if (textTag.length() > 0) {
            return "<UserLevelDocumentation>"
                    + StringUtilities.escapeForXML(textTag.toString())
                    + "</UserLevelDocumentation>";
        }
        return "";
    }

    /** Get the text of a named block tag (e.g. @author, @since). */
    private String _getBlockTagText(Element element, String tagName) {
        DocCommentTree docCommentTree = _docTrees.getDocCommentTree(element);
        if (docCommentTree == null) {
            return "";
        }
        StringBuilder textTag = new StringBuilder();
        for (DocTree docTree : docCommentTree.getBlockTags()) {
            String dtKindName = null;
            switch (docTree.getKind()) {
            case AUTHOR:
                dtKindName = "author";
                break;
            case SINCE:
                dtKindName = "since";
                break;
            case VERSION:
                dtKindName = "version";
                break;
            case UNKNOWN_BLOCK_TAG:
                dtKindName = ((UnknownBlockTagTree) docTree).getTagName();
                break;
            default:
                continue;
            }
            if (tagName.equals(dtKindName)) {
                String raw = docTree.toString();
                int firstSpace = raw.indexOf(' ');
                if (firstSpace >= 0) {
                    textTag.append(raw.substring(firstSpace + 1).trim());
                }
            }
        }
        return textTag.toString();
    }

    /** Generate the class-level documentation for a TypeElement. */
    private StringBuffer _generateClassLevelDocumentation(
            TypeElement typeElement) {
        String className = typeElement.getQualifiedName().toString();
        String shortClassName;
        if (className.lastIndexOf(".") == -1) {
            shortClassName = className;
        } else {
            shortClassName = className
                    .substring(className.lastIndexOf(".") + 1);
        }

        StringBuffer documentation = new StringBuffer(
                _header + "<doc name=\"" + shortClassName + "\" class=\""
                        + className + "\">\n" + "  <description>\n"
                        + StringUtilities
                                .escapeForXML(_inlineTagCommentText(typeElement))
                        + "  </description>\n");

        String[] classTags = { "author", "version", "since",
                "Pt.ProposedRating", "Pt.AcceptedRating",
                "UserLevelDocumentation" };
        for (String classTag : classTags) {
            String tagText = _getBlockTagText(typeElement, classTag);
            if (tagText.length() > 0) {
                documentation.append("  <" + classTag + ">"
                        + StringUtilities.escapeForXML(tagText) + "</"
                        + classTag + ">\n");
            }
        }
        return documentation;
    }

    /** Generate documentation for all fields that are derived from a
     *  specific base class.
     */
    private String _generateFieldDocumentation(TypeElement typeElement,
            Class fieldBaseClass, String xmlElement) {
        StringBuilder documentation = new StringBuilder();
        List<VariableElement> fields = ElementFilter
                .fieldsIn(typeElement.getEnclosedElements());
        for (VariableElement field : fields) {
            String fieldTypeName = field.asType().toString();

            try {
                if (fieldTypeName.equals("javax.media.j3d.Canvas3D")
                        || fieldTypeName.equals(
                                "com.sun.j3d.utils.universe.SimpleUniverse")) {
                    throw new Exception("Skipping " + fieldTypeName
                            + ", it starts up X11 and interferes with the "
                            + "nightly build");
                }
                Class type = Class.forName(fieldTypeName);
                if (fieldBaseClass.isAssignableFrom(type)) {
                    documentation.append("    <!--" + fieldTypeName + "-->\n"
                            + "    <" + xmlElement + " name=\""
                            + field.getSimpleName() + "\">"
                            + StringUtilities
                                    .escapeForXML(_inlineTagCommentText(field))
                            + _customTagCommentText(field) + "</" + xmlElement
                            + ">\n");
                }
            } catch (ClassNotFoundException ex) {
                // Ignored, probably a primitive type.
            } catch (Throwable throwable) {
                System.out.println("Failed to find class " + fieldTypeName);
                throwable.printStackTrace();
            }
        }
        return documentation.toString();
    }

    /** Given two dot separated classpath names, return a relative
     *  path to the corresponding doc file.
     */
    private static String _relativizePath(String baseDirectory,
            String destinationClassName, String baseClassName,
            boolean isField, boolean isIncluded) {
        String destinationFileName = destinationClassName.replace('.', '/');
        if (baseDirectory != null) {
            destinationFileName = baseDirectory + "/" + destinationFileName;
        }
        URI destinationURI = new File(destinationFileName).toURI();
        URI baseDirectoryURI = new File(baseDirectory).toURI();
        URI relativeURI = baseDirectoryURI.relativize(destinationURI);

        String baseClassParts[] = baseClassName.split("\\.");
        StringBuffer relativePath = new StringBuffer();

        int offset = isField ? 2 : 1;
        for (int i = 0; i < baseClassParts.length - offset; i++) {
            relativePath.append("../");
        }

        String extension = isIncluded ? ".xml" : ".html";

        if (_verbose) {
            System.out.println("PtDoclet: relativize: " + baseDirectory + " "
                    + baseClassName + " " + baseClassParts.length + " " + offset
                    + " " + relativePath + relativeURI.getPath() + extension);
        }

        return relativePath + relativeURI.getPath() + extension;
    }

    /** Write the output to a file. */
    private void _writeDoc(String className, String documentation)
            throws IOException {
        String fileBaseName = className.replace('.', File.separatorChar)
                + ".xml";

        _ptIndexer.append(className, documentation);

        if (!StringUtilities.getProperty("KEPLER").equals("")) {
            fileBaseName = className.substring(className.lastIndexOf('.') + 1)
                    + ".doc.xml";
        }

        String fileName;
        if (_outputDirectory != null) {
            fileName = _outputDirectory + File.separator + fileBaseName;
        } else {
            fileName = fileBaseName;
        }
        File directoryFile = new File(fileName).getParentFile();
        if (!directoryFile.exists()) {
            if (!directoryFile.mkdirs()) {
                throw new IOException("Directory \"" + directoryFile
                        + "\" does not exist and cannot be created.");
            }
        }
        if (_verbose) {
            System.out.println("Creating " + fileName);
        }

        FileWriter writer = new FileWriter(fileName);
        try {
            writer.write(documentation);
        } finally {
            writer.close();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private static String _header = "<?xml version=\"1.0\" standalone=\"yes\"?>\n<!DOCTYPE doc PUBLIC \"-//UC Berkeley//DTD DocML 1//EN\"\n    \"http://ptolemy.eecs.berkeley.edu/xml/dtd/DocML_1.dtd\">\n";

    private String _outputDirectory;

    private PtIndexer _ptIndexer;

    private static boolean _verbose = false;

    private Reporter _reporter;

    private DocTrees _docTrees;

    private Elements _elementUtils;

    private Types _typeUtils;

    private DocletEnvironment _environment;
}
