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
import java.io.FileNotFoundException;
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
 *  A tool to transform Java source programs to support backtracking. This
 *  transformation is done by first analyzing the Java programs, and then
 *  refactoring the program based on the information collected in the
 *  analysis phase.
 * 
 *  @author Thomas Feng
 *  @version $Id$
 *  @since Ptolemy II 4.1
 *  @Pt.ProposedRating Red (tfeng)
 *  @Pt.AcceptedRating Red (tfeng)
 */
public class Transform {
    
    /** Transform a set of files into backtracking-enabled. The set of
     *  files is given by a set of strings as their names or the names of
     *  the directories that contain them. If 
     * 
     *  @param args
     */
    public static void main(String[] args) throws Exception {
        boolean outputResult = false;
        
        if (args.length == 0)
            System.err.println("USAGE: java ptolemy.backtrack.ast.Transform " +
                    "[.java files... | directories...]");
        else {
            String[] paths = PathFinder.getPtClassPaths();
            Writer writer = outputResult ? new OutputStreamWriter(System.out) : null;
            for (int i=0; i<args.length; i++) {
                String pathOrFile = args[i];
                File[] files = PathFinder.getJavaFiles(pathOrFile, true);
                for (int j = 0; j < files.length; j++) {
                    String fileName = files[j].getPath();
                    System.err.print("Transforming \"" + fileName + "\"...");
                    
                    if (fileName.endsWith(".java")) {
                        String classFileName = 
                            fileName.substring(0, fileName.length() - 5) + ".class";
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
    
    public static void transform(char[] source, Writer writer)
            throws FileNotFoundException, IOException, ASTMalformedException {
        transform(source, writer, null);
    }
    
    public static void transform(char[] source, Writer writer, String[] classPaths)
            throws FileNotFoundException, IOException, ASTMalformedException {
        Transform transform = new Transform(source, writer, classPaths);
        transform._startTransform();
    }

    public static void transform(String fileName, Writer writer)
            throws FileNotFoundException, IOException, ASTMalformedException {
        transform(fileName, writer, null);
    }
    
    public static void transform(String fileName, Writer writer, String[] classPaths)
            throws FileNotFoundException, IOException, ASTMalformedException {
        Transform transform = new Transform(fileName, writer, classPaths);
        transform._startTransform();
        transform._outputSource();
    }

    public static TransformRule[] RULES = new TransformRule[] {
        new AssignmentRule()
    };
    
    protected void _beforeTraverse() {
        for (int i = 0; i < RULES.length; i++)
            RULES[i].beforeTraverse(_visitor, _ast);
    }
    
    protected void _outputSource() {
        ASTFormatter formatter = new ASTFormatter(_writer);
        _ast.accept(formatter);
    }
    
    protected void _parse()
            throws FileNotFoundException, IOException, ASTMalformedException {
        if (_fileName != null)
            _ast = ASTBuilder.parse(_fileName);
        else
            _ast = ASTBuilder.parse(_source);
    }
    
    protected void _startTransform()
            throws FileNotFoundException, IOException, ASTMalformedException {
        _parse();
        _beforeTraverse();
        _ast.accept(_visitor);
    }

    private Transform(char[] source, Writer writer, String[] classPaths) {
        _source = source;
        _writer = writer;
        _visitor = new TypeAnalyzer(classPaths);
    }
    
    private Transform(String fileName, Writer writer, String[] classPaths) {
        _fileName = fileName;
        _writer = writer;
        _visitor = new TypeAnalyzer(classPaths);
    }
    
    private String _fileName;
    
    private char[] _source;
    
    private Writer _writer;
    
    private CompilationUnit _ast;
    
    private TypeAnalyzer _visitor;
}
