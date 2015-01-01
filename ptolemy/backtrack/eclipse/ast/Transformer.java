/* Transform Java source programs to support backtracking.

 Copyright (c) 2005-2014 The Regents of the University of California.
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
package ptolemy.backtrack.eclipse.ast;
import java.util.Locale;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.CompilationUnit;

import ptolemy.backtrack.eclipse.ast.transform.AssignmentRule;
import ptolemy.backtrack.eclipse.ast.transform.PackageRule;
import ptolemy.backtrack.eclipse.ast.transform.TransformRule;
import ptolemy.backtrack.util.ClassFileLoader;
import ptolemy.backtrack.util.PathFinder;
import ptolemy.backtrack.util.SourceOutputStream;
import ptolemy.backtrack.util.Strings;
import ptolemy.backtrack.xmlparser.ConfigParser;
import ptolemy.backtrack.xmlparser.XmlOutput;

///////////////////////////////////////////////////////////////////
//// Transformer

/**
 A tool to transform Java source programs to support backtracking. This
 transformation is done by first analyzing the Java programs, and then
 refactoring the program based on the information collected in the
 analysis phase.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Transformer {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Transform a set of files into backtracking-enabled ones. The set of
     *  files is given by an array of strings as their names, or the names of
     *  the directories that contain them. If a string in the array is a file
     *  name, it is refactored and the output is printed out; if it is the name
     *  of a directory, all Java files in the directory and its sub-directories
     *  are looked up and refactored in the same way.
     *
     *  @param args The array of file names or names of directories.
     *  @exception Exception If any exception occures.
     */
    public static void main(String[] args) throws Exception {
        try {
            if (args.length == 0) {
                _printUsage();
            } else {
                String[] paths = PathFinder.getPtClassPaths();

                // Parse command-line options.
                int start = 0;

                while (start < args.length) {
                    int newPosition = parseArguments(args, start);

                    if (newPosition != start) {
                        start = newPosition;
                    } else {
                        break;
                    }
                }

                if (_extraClassPaths != null) {
                    paths = Strings.combineArrays(paths, _extraClassPaths);
                }

                // Set up the list of file names.
                List<File> fileList = new LinkedList<File>();
                Set<String> crossAnalysis = new HashSet<String>();

                for (int i = start; i < args.length; i++) {
                    String pathOrFile = args[i];
                    File[] files;

                    if (pathOrFile.startsWith("@")) {
                        // A file list.
                        // Each line in the file contains a single file name.
                        String listName = pathOrFile.substring(1);
                        File listFile = new File(listName);
                        File listPath = listFile.getParentFile();
                        List<String> strings = new LinkedList<String>();
                        BufferedReader reader = null;
                        try {
                            reader = new BufferedReader(
                                new InputStreamReader(new FileInputStream(
                                        listName)));
                            String line = reader.readLine();

                            while (line != null) {
                                strings.add(new File(listPath, line)
                                            .getCanonicalPath());
                                line = reader.readLine();
                            }
                        } finally {
                            if (reader != null) {
                                reader.close();
                            }
                        }
                        files = new File[strings.size()];

                        Iterator<String> stringsIter = strings.iterator();

                        for (int j = 0; stringsIter.hasNext(); j++) {
                            files[j] = new File(stringsIter.next());
                        }
                    } else {
                        files = PathFinder.getJavaFiles(pathOrFile, true);
                    }

                    ClassFileLoader loader = null;
                    //try {
                        loader = new ClassFileLoader(paths);

                        for (int j = 0; j < files.length; j++) {
                            String fileName = files[j].getPath();

                            if (fileName.endsWith(".java")) {
                                fileName = fileName.substring(0,
                                                              fileName.length() - 5)
                                    + ".class";
                            } else {
                                System.err.println("Skipping \"" + files[j]
                                                   + "\". " + "Cause: Class file not found.");
                                continue;
                            }

                            Class c = null;

                            try {
                                c = loader.loadClass(new File(fileName));
                            } catch (Throwable throwable) {
                                /*System.err.println("Skipping \"" + files[j] + "\". "
                                  + "Cause: " + e.getMessage());
                                  continue;*/
                                System.err.println("***********************");
                                String message = throwable.getMessage();
                                System.err
                                    .println("Cannot load class from file: \""
                                             + fileName + "\": " + message);

                                String header = "Prohibited package name:";
                                if (message.startsWith(header)) {
                                    String packageName = message.substring(
                                                                           header.length()).trim();
                                    String name = new File(fileName).getName();
                                    int dotPos = name.indexOf('.');
                                    if (dotPos >= 0) {
                                        name = name.substring(0, dotPos);
                                    }
                                    String className = packageName + "." + name;
                                    System.err
                                        .println("Try to use preloaded class: "
                                                 + className);
                                    try {
                                        c = loader.loadClass(className);
                                    } catch (Exception e2) {
                                    }

                                    if (c == null) {
                                        System.err
                                            .println("Cannot obtain preloaded class: "
                                                     + className);
                                        continue;
                                    }
                                }
                            }

                            fileList.add(files[j]);
                            if (c == null) {
                                throw new NullPointerException("Could not obtain "
                                                               + "preloaded class \"" + fileName + "\"");
                            }
                            crossAnalysis.add(c.getName());
                            _addInnerClasses(crossAnalysis, fileName, (c
                                                                       .getPackage() == null) ? null : c.getPackage()
                                             .getName());
                        }
                // java.net.URLClassLoader is not present in Java 1.6.
//                     } finally {
//                         if (loader != null) {
//                             loader.close();
//                         }
//                     }
                }

                // Compute the array of cross-analyzed types.
                String[] crossAnalyzedTypes = new String[crossAnalysis.size()];
                Iterator<String> crossAnalysisIter = crossAnalysis.iterator();

                for (int i = 0; crossAnalysisIter.hasNext(); i++) {
                    crossAnalyzedTypes[i] = crossAnalysisIter.next();
                }

                Writer standardWriter = _defaultToStandardOutput ? new OutputStreamWriter(
                        System.out)
                        : null;

                // Handle files.
                Iterator<File> filesIter = fileList.iterator();

                while (filesIter.hasNext()) {
                    File file = filesIter.next();
                    String fileName = file.getPath();
                    System.err.println("Transforming \"" + fileName + "\"...");

                    transform(file.getPath(), standardWriter, paths,
                            crossAnalyzedTypes);

                    if (_defaultToStandardOutput) {
                        standardWriter.flush();
                    }
                }

                _outputConfig();

                if (_defaultToStandardOutput) {
                    standardWriter.close();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(2);
        }
    }

    /** Parse the command-line arguments starting from the given position.
     *  If one or more argument corresponds to an option, proper actions are
     *  performed to record that option. The position is adjusted to the next
     *  file name or option and returned.
     *
     *  @param args The command-line arguments.
     *  @param position The starting position.
     *  @return The new position.
     */
    public static int parseArguments(String[] args, int position) {
        String arg = args[position];

        if (arg.equals("-classpath") || arg.equals("-cp")) {
            position++;

            String classPaths = args[position];
            _extraClassPaths = Strings.combineArrays(_extraClassPaths, Strings
                    .decodeFileNames(classPaths));
            position++;
        } else if (arg.equals("-prefix") || arg.equals("-p")) {
            position++;
            _prefix = args[position];

            for (int i = 0; i < RULES.length; i++) {
                if (RULES[i] instanceof PackageRule) {
                    ((PackageRule) RULES[i]).setPrefix(_prefix);
                    break;
                }
            }

            position++;
            _defaultToStandardOutput = false;
        } else if (arg.equals("-output") || arg.equals("-o")) {
            position++;
            _rootPath = args[position];

            if (_rootPath.length() == 0) {
                _rootPath = ".";
            }

            position++;
        } else if (arg.equals("-overwrite") || arg.equals("-w")) {
            position++;
            _overwrite = true;
        } else if (arg.equals("-nooverwrite") || arg.equals("-nw")) {
            position++;
            _overwrite = false;
        } else if (arg.equals("-config") || arg.equals("-c")) {
            position++;
            _configName = args[position];
            position++;
        }

        return position;
    }

    /** Transform the AST with given class paths, and output the result to
     *  the writer.
     *  <p>
     *  If a output directory is set with the <tt>-output</tt>
     *  command-line argument, the output is written to a Java source
     *  file with that directory as the root directory. The given
     *  writer is not used in that case.
     *
     *  @param fileName The Java file name.
     *  @param ast The AST to be refactored.
     *  @param writer The writer where output is written.
     *  @param classPaths The class paths.
     *  @param crossAnalyzedTypes The array of names of types to be added to
     *   the visitor's cross-analyzed types list.
     *  @exception IOException If IO exception occurs when reading from
     *   the Java file or riting to the output.
     *  @exception ASTMalformedException If the Java source is illegal.
     *  @see #transform(String, Writer)
     */
    public static void transform(String fileName, CompilationUnit ast,
            Writer writer, String[] classPaths, String[] crossAnalyzedTypes)
            throws IOException, ASTMalformedException {
        boolean needClose = false;

        Transformer transform = new Transformer(fileName, classPaths);

        if (ast == null) {
            transform._parse();
        } else {
            transform._ast = ast;
        }

        if (crossAnalyzedTypes != null) {
            transform._visitor.addCrossAnalyzedTypes(crossAnalyzedTypes);
        }

        transform._startTransform();

        if (writer == null) {
            String packageName = transform._ast.getPackage().getName()
                    .toString();
            File file = new File(fileName);
            String outputFileName = file.getName();
            SourceOutputStream outputStream = SourceOutputStream.getStream(
                    _rootPath, packageName, outputFileName, _overwrite);
            writer = new OutputStreamWriter(outputStream, java.nio.charset.Charset.defaultCharset());
            needClose = true;
        }

        transform._outputSource(writer, fileName);

        if (needClose) {
            writer.close();
        }

        // Record the class name.
        if (_configName != null) {
            File file = new File(fileName);
            String simpleName = file.getName();

            if (simpleName.toUpperCase(Locale.getDefault()).endsWith(".JAVA")) {
                String baseName = simpleName.substring(0,
                        simpleName.length() - 5);
                CompilationUnit root = (CompilationUnit) transform._ast
                        .getRoot();
                String className;

                if (root.getPackage() != null) {
                    className = root.getPackage().getName().toString() + "."
                            + baseName;
                } else {
                    className = baseName;
                }

                if ((_prefix != null) && (_prefix.length() > 0)) {
                    className = className.substring(_prefix.length() + 1);
                }

                _classes.add(className);
            }
        }
    }

    /** Transform the Java source in the file given by its name with no
     *  explicit class path, and output the result to the writer. This
     *  is the same as calling <tt>transform(fileName, writer, null)</tt>.
     *
     *  @param fileName The Java file name.
     *  @param writer The writer where output is written.
     *  @exception IOException If IO exception occurs when reading from
     *   the Java file or riting to the output.
     *  @exception ASTMalformedException If the Java source is illegal.
     *  @see #transform(String, Writer, String[])
     */
    public static void transform(String fileName, Writer writer)
            throws IOException, ASTMalformedException {
        transform(fileName, writer, null);
    }

    /** Transform the Java source in the file given by its name with
     *  given class paths, and output the result to the writer.
     *  <p>
     *  If a output directory is set with the <tt>-output</tt>
     *  command-line argument, the output is written to a Java source
     *  file with that directory as the root directory. The given
     *  writer is not used in that case.
     *
     *  @param fileName The Java file name.
     *  @param writer The writer where output is written.
     *  @param classPaths The class paths.
     *  @exception IOException If IO exception occurs when reading from
     *   the Java file or riting to the output.
     *  @exception ASTMalformedException If the Java source is illegal.
     *  @see #transform(String, Writer)
     */
    public static void transform(String fileName, Writer writer,
            String[] classPaths) throws IOException, ASTMalformedException {
        transform(fileName, writer, classPaths, null);
    }

    /** Transform the Java source in the file given by its name with
     *  given class paths, and output the result to the writer.
     *  <p>
     *  If a output directory is set with the <tt>-output</tt>
     *  command-line argument, the output is written to a Java source
     *  file with that directory as the root directory. The given
     *  writer is not used in that case.
     *
     *  @param fileName The Java file name.
     *  @param writer The writer where output is written.
     *  @param classPaths The class paths.
     *  @param crossAnalyzedTypes The array of names of types to be added to
     *   the visitor's cross-analyzed types list.
     *  @exception IOException If IO exception occurs when reading from
     *   the Java file or riting to the output.
     *  @exception ASTMalformedException If the Java source is illegal.
     *  @see #transform(String, Writer)
     */
    public static void transform(String fileName, Writer writer,
            String[] classPaths, String[] crossAnalyzedTypes)
            throws IOException, ASTMalformedException {
        transform(fileName, null, writer, classPaths, crossAnalyzedTypes);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       public fields                       ////

    /** The refactoring rules to be sequentially applied to the source code.
     */
    public static TransformRule[] RULES = new TransformRule[] {
            new AssignmentRule(), new PackageRule() };

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Call the <tt>afterTraverse</tt> of all the refactoring rules. For
     *  each rule, the <tt>afterTraverse</tt> is called after the AST is
     *  traversed. The rule may clean up states or finalize the
     *  transformation.
     */
    protected void _afterTraverse() {
        for (int i = 0; i < RULES.length; i++) {
            RULES[i].afterTraverse(_visitor, _ast);
        }
    }

    /** Call the <tt>beforeTraverse</tt> of all the refactoring rules. For
     *  each rule, the <tt>beforeTraverse</tt> is called before the AST is
     *  traversed. The rule may set up handlers for special events in the
     *  traversal.
     */
    protected void _beforeTraverse() {
        for (int i = 0; i < RULES.length; i++) {
            RULES[i].beforeTraverse(_visitor, _ast);
        }
    }

    /** Output XML configuration to the pre-defined file (specified with
     *  "-config" argument in {@link #main(String[])}).
     *
     *  @exception Exception If any error occurs.
     */
    protected static void _outputConfig() throws Exception {
        if (_configName != null) {
            // Remove the configuration.
            SourceOutputStream stream = SourceOutputStream.getStream(
                    _configName, _overwrite);
            Set<String> classSet = new HashSet<String>();
            classSet.addAll(_classes);

            ConfigParser parser = new ConfigParser();
            parser.addExcludedFile(new File(_configName).getCanonicalPath());
            parser.parseConfigFile(ConfigParser.DEFAULT_SYSTEM_ID, classSet);

            if ((_prefix != null) && (_prefix.length() > 0)) {
                parser.addPackagePrefix(_prefix, classSet);
            }

            OutputStreamWriter writer = null;
            try {
                writer = new OutputStreamWriter(stream, java.nio.charset.Charset.defaultCharset());
                XmlOutput.outputXmlTree(parser.getTree(), writer);
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }
    }

    /** Output the Java source from the current AST with {@link ASTFormatter}.
     *
     *  @param writer The writer where the output is written to.
     *  @param fileName The file name of the writer.
     *  @exception IOException If error occurs while writing to the writer.
     */
    protected void _outputSource(Writer writer, String fileName)
            throws IOException {
        FileInputStream stream = new FileInputStream(fileName);
        ASTFormatter formatter = new ASTFormatter(writer, stream);
        _ast.accept(formatter);
        stream.close();
    }

    /** Parse the Java source and retrieve the AST. If a source file name is
     *  given, the file is read and parsed; if the source is given in the form
     *  of <tt>char[]</tt>, it is directly passed to the parser.
     *
     *  @exception IOException If a file name is given, but exception
     *   occurs when reading from the file.
     *  @exception ASTMalformedException If the source is illegal.
     */
    protected void _parse() throws IOException, ASTMalformedException {
        if (_fileName != null) {
            _ast = ASTBuilder.parse(_fileName);
        }
    }

    /** Start the transformation by first parsing the source with
     *  {@link #_parse()}, then call {@link #_beforeTraverse()},
     *  then traverse the AST with {@link TypeAnalyzer} visitor,
     *  and finally call {@link #_afterTraverse()}.
     *
     *  @exception IOException If a file name is given, but exception
     *   occurs when reading from the file.
     *  @exception ASTMalformedException If the source is illegal.
     *  @see #_afterTraverse()
     *  @see #_beforeTraverse()
     *  @see #_parse()
     */
    protected void _startTransform() throws IOException, ASTMalformedException {
        _beforeTraverse();
        _ast.accept(_visitor);
        _afterTraverse();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Construct a transformer. This constructor should not be called from
     *  outside of this class.
     *
     *  @param fileName The Java source file name.
     *  @param classPaths An array of explicit class paths, or <tt>null</tt>
     *   if none.
     *
     *  @exception  MalformedURLException If a classpath is not a proper URL.
     */
    private Transformer(String fileName, String[] classPaths)
            throws MalformedURLException {
        _fileName = fileName;
        _visitor = new TypeAnalyzer(classPaths);
    }

    /** Add an inner class to the cross-analysis set.
     *
     *  @param crossAnalysis The cross-analysis set.
     *  @param classFileName The file name of the inner class to be added.
     *  @param packageName The name of the package that the inner class is in.
     */
    private static void _addInnerClasses(Set<String> crossAnalysis,
            String classFileName, String packageName) {
        File topFile = new File(classFileName);
        File path = topFile.getParentFile();

        if (path == null) {
            path = new File(".");
        }

        String className = topFile.getName().substring(0,
                topFile.getName().length() - 6);
        File[] files = path.listFiles(new InnerClassFilter(className));

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            className = file.getName()
                    .substring(0, file.getName().length() - 6);

            if (packageName != null) {
                className = packageName + "." + className;
            }

            crossAnalysis.add(className);
        }
    }

    /** Print the command-line usage of the transformer.
     */
    private static void _printUsage() {
        System.err
                .println("USAGE: java ptolemy.backtrack.eclipse.ast.Transform");
        System.err.println("           " + "[options] "
                + "[java_files | directories | @file_lists]");
        System.err.println();
        System.err.println("Options:");
        System.err.println("          -classpath <paths> "
                + "add extra class path(s)");
        System.err.println("          -config <file>     "
                + "save the configuration in a new file");
        System.err.println("          -nooverwrite       "
                + "do not overwrite existing Java files (default)");
        System.err.println("          -output <root>     "
                + "root directory of output files");
        System.err.println("          -overwrite         "
                + "overwrite existing Java files");
        System.err.println("          -prefix <name>     "
                + "prefix to be added to the package names");
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                      ////

    /** The AST. Not <tt>null</tt> after {@link #_parse()} is successfully
     *  called.
     */
    private CompilationUnit _ast;

    /** Class names of all the source parsed.
     */
    private static List<String> _classes = new LinkedList<String>();

    /** The name of the output XML configuration.
     */
    private static String _configName;

    /** Whether the default output is the standard output. If it is
     *  <tt>true</tt> and no "-prefix" parameter is given, the result of
     *  refactoring is printed to the console; otherwise, files are created
     *  for the output.
     */
    private static boolean _defaultToStandardOutput = true;

    /** Extra classpaths to resolve classes.
     */
    private static String[] _extraClassPaths = new String[0];

    /** The Java source file name, if not <tt>null</tt>.
     */
    private String _fileName;

    /** Whether to overwrite existing file(s).
     */
    private static boolean _overwrite = false;

    /** The prefix to be added to the package name of the source.
     */
    private static String _prefix;

    /** The root directory of the Java source output.
     */
    private static String _rootPath;

    /** The visitor used to traverse the AST.
     */
    private TypeAnalyzer _visitor;

    ///////////////////////////////////////////////////////////////////
    ////                        nested class                       ////

    ///////////////////////////////////////////////////////////////////
    //// InnerClassFilter

    /**
     File name filter for inner classes in the given class. Inner classes
     are saved in class files with names containing "$".

     @author Thomas Feng
     @version $Id$
     @since Ptolemy II 5.1
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    private static class InnerClassFilter implements FilenameFilter {
        /** Test whether a file in a directory is accepted.
         *
         *  @param dir The directory where the file is in.
         *  @param name The simple name of the file.
         *  @return <tt>true</tt> if the file is accepted; <tt>false</tt>
         *   otherwise.
         */
        public boolean accept(File dir, String name) {
            return name.startsWith(_className + "$") && name.endsWith(".class");
        }

        /** Construct an inner class filter.
         *
         *  @param className The name of the class whose inner classes (if any)
         *   are looked for.
         */
        InnerClassFilter(String className) {
            _className = className;
        }

        /** The name of the given class whose inner classes (if any) are looked
         *  for.
         */
        private String _className;
    }
}
