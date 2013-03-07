/* The class to create Eclipse ASTs from Java source files.

 Copyright (c) 2005-2013 The Regents of the University of California.
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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

///////////////////////////////////////////////////////////////////
//// ASTBuilder

/**
 Static methods that build Eclipse Abstract Syntax Trees (ASTs)
 from Java source files.
 <p>
 Currently only Java 1.4 source files are accepted.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ASTBuilder {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the ID of the Java language specification being used by the
     *  parser.
     *
     *  @return The ID of the Java language specification.
     *  @see #setLanguageSpecification(int)
     */
    public static int getLanguageSpecification() {
        return DEFAULT_LANGUAGE_SPECIFICATION;
    }

    /** Parse a Java source file given by its name, and return the
     *  root of the AST.
     *
     *  @param fileName The Java source file name.
     *  @return The root of the AST.
     *  @exception IOException If IO exception occurs.
     *  @exception ASTMalformedException If the Java source file
     *   does not conform to the supported Java grammar.
     *  @see #parse(char[])
     */
    public static CompilationUnit parse(String fileName) throws IOException,
            ASTMalformedException {
        File file = new File(fileName);
        long fileLength = file.length();
        char[] source = new char[(int) fileLength];
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(file);
            int bytesRead;
            if ((bytesRead = fileReader.read(source)) != fileLength) {
                throw new IOException("While reading \"" + fileName
                        + ", read only " + bytesRead + ", expecting "
                        + fileLength + "bytes.");
            }
        } finally {
            if (fileReader != null) {
                fileReader.close();
            }
        }

        try {
            return parse(source);
        } catch (ASTMalformedException e) {
            throw new ASTMalformedException(fileName);
        }
    }

    /** Parse the Java source code given in the source buffer, and
     *  return the root of the AST.
     *
     *  @param source The <tt>char</tt> array that contains the
     *   source code in a single Java source file.
     *  @return The root of the AST.
     *  @exception ASTMalformedException If the Java source file
     *   does not conform to the supported Java grammar.
     */
    public static CompilationUnit parse(char[] source)
            throws ASTMalformedException {
        ASTParser parser = ASTParser.newParser(DEFAULT_LANGUAGE_SPECIFICATION);
        parser.setSource(source);

        CompilationUnit ast = (CompilationUnit) parser.createAST(null);

        if ((ast.getFlags() & ASTNode.MALFORMED) != 0) {
            throw new ASTMalformedException();
        }

        return ast;
    }

    /** Set the ID of the Java language specification being used by the
     *  parser.
     *  <p>
     *  Users may change the language specification <em>before</em> Java source
     *  is parsed. The language specification is set statically. It is users'
     *  responsibility to synchronize its usage if multiple threads parse Java
     *  sources at the same time with different language specifications.
     *
     *  @param level The ID of the Java language specification.
     *  @see #getLanguageSpecification()
     */
    public static void setLanguageSpecification(int level) {
        DEFAULT_LANGUAGE_SPECIFICATION = level;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       public fields                       ////

    /** The ID of the Java language specification being used by the parser.
     *
     *  @see #getLanguageSpecification()
     *  @see #setLanguageSpecification(int)
     */
    public static int DEFAULT_LANGUAGE_SPECIFICATION = AST.JLS3;
}
