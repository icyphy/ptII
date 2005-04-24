/* Dump the structure of an Eclipse AST.

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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;

//////////////////////////////////////////////////////////////////////////
//// ASTDump
/**
   An Eclipse AST (Abstract Syntax Tree) visitor that traverses an AST and
   outputs its structure.
 
   @author Thomas Feng
   @version $Id$
   @since Ptolemy II 5.1
   @Pt.ProposedRating Red (tfeng)
   @Pt.AcceptedRating Red (tfeng)
*/
public class ASTDump extends ASTVisitor {

    /** Construct an AST dump with a {@link StringBuffer} where the
     *  output will be added.
     *
     *  @param buffer The string buffer to be used.
     */
    public ASTDump(StringBuffer buffer) {
        _buffer = buffer;
    }

    /** Construct an AST dump with a writer to which the
     *  output will be written.
     *
     *  @param writer The writer to write to.
     */
    public ASTDump(Writer writer) {
        _writer = writer;
    }

    /** Read in one or more Java source files, parse them with the
     *  Eclipse parser, and output their AST structure to standard
     *  output.
     *
     *  @param args The names of Java source files.
     */
    public static void main(String[] args) throws Exception {
        if (args.length == 0)
            System.err.println("USAGE: java ptolemy.backtrack.ast.ASTDump" +
                    " [.java files...]");
        else {
            Writer writer = new OutputStreamWriter(System.out);
            for (int i = 0; i < args.length; i++) {
                String fileName = args[i];
                CompilationUnit root = ASTBuilder.parse(fileName);
                ASTDump dump = new ASTDump(writer);
                root.accept(dump);
            }
            writer.close();
        }
    }

    /** End the visiting of a node (and all its children), and decrease
     *  the indent amount.
     *
     *  @param node The node that have been visited.
     */
    public void postVisit(ASTNode node) {
        _decreaseIndent();
        super.postVisit(node);
    }

    /** Visit a node in the AST and output it. If the node is an {@link
     *  Expression}, its content is also output. If the AST has been
     *  type-checked with {@link TypeAnalyzer}, the type (if any)
     *  associated with each node is also output.
     *
     *  @param node The node to be visited.
     */
    public void preVisit(ASTNode node) {
        if (node instanceof AbstractTypeDeclaration)
            TypeAnalyzer._sortBodyDeclarations((AbstractTypeDeclaration)node);
        else if (node instanceof AnonymousClassDeclaration)
            TypeAnalyzer._sortBodyDeclarations((AnonymousClassDeclaration)node);

        _output(_indent);

        _output(_getShortName(node.getClass()));

        Type type = Type.getType(node);
        if (type != null) {
            _output(":");
            _output(type.getName());
        }

        if (node instanceof Expression) {
            _output(" (");
            _output(node.toString());
            _output(")");
        }

        _output("\n");
        _increaseIndent();
        super.preVisit(node);
    }

    /** Decrease the current indentation by a unit (four spaces).
     */
    private void _decreaseIndent() {
        _indent.setLength(_indent.length() - 4);
    }

    /** Get the simple name (the last part of its full name, without any
     *  '$' or '.' in it) of a {@link Class} object.
     *
     *  @param c The class object.
     *  @return The simple name.
     */
    private static String _getShortName(Class c) {
        String fullName = c.getName();
        int pos = fullName.lastIndexOf('$');
        pos = pos == -1 ? fullName.lastIndexOf('.') : pos;
        if (pos == -1)
            return fullName;
        else
            return fullName.substring(pos + 1);
    }

    /** Increase the current indentation by a unit (four spaces).
     */
    private void _increaseIndent() {
        _indent.append("    ");
    }

    /** Output a message. If a {@link StringBuffer} is used, the output
     *  is appended to the buffer; if a {@link Writer} is provided, the
     *  output is written to the writer.
     *
     *  @param message The message to be output.
     *  @exception ASTIORuntimeException If a writer is provided but
     *   IO exception occurs when trying to write to the writer.
     */
    private void _output(String message) throws ASTIORuntimeException {
        if (_buffer != null)
            _buffer.append(message);
        if (_writer != null)
            try {
                _writer.write(message);
            } catch (IOException e) {
                throw new ASTIORuntimeException(e);
            }
    }

    /** Output a message. If a {@link StringBuffer} is used, the output
     *  is appended to the buffer; if a {@link Writer} is provided, the
     *  output is written to the writer.
     *
     *  @param message The message to be output.
     *  @exception ASTIORuntimeException If a writer is provided but
     *   IO exception occurs when trying to write to the writer.
     */
    private void _output(StringBuffer message) throws ASTIORuntimeException {
        if (_buffer != null)
            _buffer.append(message);
        if (_writer != null)
            try {
                _writer.write(message.toString());
            } catch (IOException e) {
                throw new ASTIORuntimeException(e);
            }
    }

    /** The current indentation, a string of spaces.
     */
    private StringBuffer _indent = new StringBuffer();

    /** The string buffer, where the output is added to.
     */
    private StringBuffer _buffer;

    /** The writer, where the output is written to.
     */
    private Writer _writer;
}
