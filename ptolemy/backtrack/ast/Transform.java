/* Transform Java source programs to support backtracking.

Copyright (c) 2005 The Regents of the University of California.
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

package ptolemy.backtrack.ast;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.eclipse.jdt.core.dom.CompilationUnit;

import ptolemy.backtrack.ast.transform.AssignmentRule;
import ptolemy.backtrack.ast.transform.TransformRule;
import ptolemy.backtrack.util.PathFinder;

//////////////////////////////////////////////////////////////////////////
//// Transform
/**
   A tool to transform Java source programs to support backtracking. This
   transformation is done by first analyzing the Java programs, and then
   refactoring the program based on the information collected in the
   analysis phase.
  
   @author Thomas Feng
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (tfeng)
   @Pt.AcceptedRating Red (tfeng)
*/
public class Transform {
    
    /** Transform a set of files into backtracking-enabled ones. The set of
     *  files is given by an array of strings as their names, or the names of
     *  the directories that contain them. If a string in the array is a file
     *  name, it is refactored and the output is printed out; if it is the name
     *  of a directory, all Java files in the directory and its sub-directories
     *  are looked up and refactored in the same way.
     * 
     *  @param args The array of file names or names of directories.
     *  @exception Exception If any exception occues.
     */
    public static void main(String[] args) throws Exception {
        boolean outputResult = false;
        
        if (args.length == 0)
            System.err.println("USAGE: java ptolemy.backtrack.ast.Transform " +
                    "[.java files... | directories...]");
        else {
            String[] paths = PathFinder.getPtClassPaths();
            Writer writer = 
                outputResult ? new OutputStreamWriter(System.out) : null;
            for (int i = 0; i < args.length; i++) {
                String pathOrFile = args[i];
                File[] files = PathFinder.getJavaFiles(pathOrFile, true);
                for (int j = 0; j < files.length; j++) {
                    String fileName = files[j].getPath();
                    System.err.print("Transforming \"" + fileName + "\"...");
                    
                    if (fileName.endsWith(".java")) {
                        String classFileName = 
                            fileName.substring(0, fileName.length() - 5) +
                            ".class";
                        if (new File(classFileName).exists())
                            System.err.println();
                        else {
                            System.err.println(" SKIP");
                            continue;
                        }
                    } else
                        continue;
                    
                    System.err.flush();
                    transform(files[j].getPath(), writer, paths);
                    if (outputResult)
                        writer.flush();
                }
            }
            if (outputResult)
                writer.close();
        }
    }
    
    /** Transform the Java source with no explicit class path, and
     *  output the result to the writer. This is the same as calling
     *  <tt>transform(source, writer, null)</tt>.
     * 
     *  @param source The Java source.
     *  @param writer The writer where output is written.
     *  @exception IOException If IO exception occurs when writing to
     *   the output.
     *  @exception ASTMalformedException If the Java source is illegal.
     *  @see #transform(char[], Writer, String[])
     */
    public static void transform(char[] source, Writer writer)
            throws IOException, ASTMalformedException {
        transform(source, writer, null);
    }
    
    /** Transform the Java source with given classpaths, and output the
     *  result to the writer.
     * 
     *  @param source The Java source.
     *  @param writer The writer where output is written.
     *  @param classPaths The class paths.
     *  @exception IOException If IO exception occurs when writing to the
     *   output.
     *  @exception ASTMalformedException If the Java source is illegal.
     *  @see #transform(char[], Writer)
     */
    public static void transform(char[] source, Writer writer, 
            String[] classPaths)
            throws IOException, ASTMalformedException {
        Transform transform = new Transform(source, writer, classPaths);
        transform._startTransform();
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
            String[] classPaths)
            throws IOException, ASTMalformedException {
        Transform transform = new Transform(fileName, writer, classPaths);
        transform._startTransform();
        transform._outputSource();
    }

    /** The refactoring rules to be sequentially applied to the source code.
     */
    public static TransformRule[] RULES = new TransformRule[] {
        new AssignmentRule()
    };
    
    /** Call the <tt>afterTraverse</tt> of all the refactoring rules. For
     *  each rule, the <tt>afterTraverse</tt> is called after the AST is
     *  traversed. The rule may clean up states or finalize the
     *  transformation.
     */
    protected void _afterTraverse() {
        for (int i = 0; i < RULES.length; i++)
            RULES[i].afterTraverse(_ast);
    }
    
    /** Call the <tt>beforeTraverse</tt> of all the refactoring rules. For
     *  each rule, the <tt>beforeTraverse</tt> is called before the AST is
     *  traversed. The rule may set up handlers for special events in the
     *  traversal.
     */
    protected void _beforeTraverse() {
        for (int i = 0; i < RULES.length; i++)
            RULES[i].beforeTraverse(_visitor, _ast);
    }
    
    /** Output the Java source from the current AST with {@link ASTFormatter}.
     */
    protected void _outputSource() {
        ASTFormatter formatter = new ASTFormatter(_writer);
        _ast.accept(formatter);
    }
    
    /** Parse the Java source and retrieve the AST. If a source file name is
     *  given, the file is read and parsed; if the source is given in the form
     *  of <tt>char[]</tt>, it is directly passed to the parser.
     *  
     *  @exception IOException If a file name is given, but exception
     *   occurs when reading from the file.
     *  @exception ASTMalformedException If the source is illegal.
     */
    protected void _parse()
            throws IOException, ASTMalformedException {
        if (_fileName != null)
            _ast = ASTBuilder.parse(_fileName);
        else
            _ast = ASTBuilder.parse(_source);
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
    protected void _startTransform()
            throws IOException, ASTMalformedException {
        _parse();
        _beforeTraverse();
        _ast.accept(_visitor);
        _afterTraverse();
    }

    /** Construct a transformer. This constructor should not be called from
     *  outside of this class.
     * 
     *  @param source The Java source.
     *  @param writer The writer where the output is written.
     *  @param classPaths An array of explicit class paths, or <tt>null</tt>
     *   if none.
     */
    private Transform(char[] source, Writer writer, String[] classPaths) {
        _source = source;
        _writer = writer;
        _visitor = new TypeAnalyzer(classPaths);
    }
    
    /** Construct a transformer. This constructor should not be called from
     *  outside of this class.
     * 
     *  @param fileName The Java source file name.
     *  @param writer The writer where the output is written.
     *  @param classPaths An array of explicit class paths, or <tt>null</tt>
     *   if none.
     */
    private Transform(String fileName, Writer writer, String[] classPaths) {
        _fileName = fileName;
        _writer = writer;
        _visitor = new TypeAnalyzer(classPaths);
    }
    
    /** The Java source file name, if not <tt>null</tt>.
     */
    private String _fileName;
    
    /** The Java source, if not <tt>null</tt>
     */
    private char[] _source;
    
    /** The output writer.
     */
    private Writer _writer;
    
    /** The AST. Not <tt>null</tt> after {@link #_parse()} is successfully
     *  called.
     */
    private CompilationUnit _ast;
    
    /** The visitor used to traverse the AST.
     */
    private TypeAnalyzer _visitor;
}
