/* A set of AST query functions.

Copyright (c) 1998-2004 The Regents of the University of California.
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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sourceforge.jrefactory.ast.ASTCompilationUnit;
import net.sourceforge.jrefactory.ast.Node;
import net.sourceforge.jrefactory.parser.JavaParserVisitor;

import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.SummaryLoadVisitor;
import org.acm.seguin.summary.SummaryLoaderState;

//////////////////////////////////////////////////////////////////////////
//// ASTQuery
/**
   An AST (Abstract Syntax Tree) represents the information abstracted from
   a source program. This class provides a set of general methods to query
   ASTs. These methods are all static and stateless. They can be applied to
   any AST obtained from the JRefactory parser.

   @author Thomas Feng
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (tfeng)
*/
public class ASTQuery {
    
    ///////////////////////////////////////////////////////////////////
    ////                       public methods                      ////
    
    /** Dump the content of the AST starting at the given node to
     *  standard output.
     *  
     *  @param node The root of the AST to be dumped.
     */
    public static void dumpAST(Node node) {
        try {
            dumpAST(node, new OutputStreamWriter(System.out));
        } catch (IOException e) {
        }
    }
    
    /** Dump the content of the AST starting at the given node to
     *  the specified writer.
     *  
     *  @param node The node from which the AST is dumped
     *   recursively.
     *  @param writer The writer to be dumped to. It must have been
     *   opened by the user, and it should be closed by the user
     *   later.
     */
    public static void dumpAST(Node node, Writer writer)
            throws IOException {
        dumpAST(node, writer, 0);
        writer.flush();
    }

    /** Return a given amount of white spaces as a string.
     * 
     *  @param indent The indent amount in the number of spaces.
     */
    public static String getIndentString(int indent) {
        StringBuffer buffer = new StringBuffer(indent);
        for (int i=0; i<indent; i++)
            buffer.append(' ');
        return buffer.toString();
    }
    
    /** Summarize an AST from by traversing it from the root.
     *  It is the same as calling <tt>summarize(root, null)</tt>.
     *  
     *  @param root Root of an AST.
     *  @return The summary.
     */
    public static FileSummary summarize(ASTCompilationUnit root) {
        return summarize(root, null);
    }
    
    /** Summarize an AST from by traversing it from the root. If
     *  the file name given is not <tt>null</tt>, it is set to be
     *  the source file name of the resulting summary.
     *  
     *  @param root Root of an AST.
     *  @param fileName The name of the source file from which the
     *   AST is built.
     *  @return The summary.
     */
    public static FileSummary summarize(ASTCompilationUnit root, String fileName) {
        SummaryLoaderState state = new SummaryLoaderState();
        if (fileName != null)
            state.setFile(new File(fileName));
        SummaryLoadVisitor visitor = new SummaryLoadVisitor();
        root.jjtAccept(visitor, state);
        return (FileSummary)state.getCurrentSummary();
    }
    
    /** Summarize an AST from by traversing it from the root with
     *  a user specified visitor as the summary builder. If
     *  the file name given is not <tt>null</tt>, it is set to be
     *  the source file name of the resulting summary.
     *  <p>
     *  
     *  @param root Root of an AST.
     *  @param fileName The name of the source file from which the
     *   AST is built.
     *  @param visitor The customized visitor, usually of a subclass
     *   of {@link TraversalVisitor}.
     *  @return The summary.
     */
    public static FileSummary summarize(ASTCompilationUnit root, String fileName, JavaParserVisitor visitor) {
        SummaryLoaderState state = new SummaryLoaderState();
        if (fileName != null)
            state.setFile(new File(fileName));
        root.jjtAccept(visitor, state);
        return (FileSummary)state.getCurrentSummary();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                       public fields                       ////

    /** Whether to dump the <tt>image</tt> property of AST nodes.
     */
    public static boolean DUMP_IMAGE = true;
    
    /** Whether to indicate the <tt>specials</tt> table if any
     *  AST node as it.
     */
    public static boolean DUMP_SPECIALS = false;
    
    /** Whether to indicate the type-checking result for AST
     *  nodes. The AST must be type-checked with {@link
     *  TypeAnalyzer} first.
     */
    public static boolean DUMP_TYPE = true;

    ///////////////////////////////////////////////////////////////////
    ////                         constructor                       ////

    /** Construct an object. This class should not be instantiated.
     *  Users should directly call its static functions. This constructor
     *  is set to private.
     */
    private ASTQuery() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private methods                     ////

    /** Recusive function for {@link #dumpAST(Node, Writer)}.
     * 
     *  @param node The node from which the AST is dumped
     *   recursively.
     *  @param writer The writer to be dumped to. It must have been
     *   opened by the user, and it should be closed by the user
     *   later.
     *  @param indent The amount of the current indent.
     *  @see {@link #dumpAST(Node, Writer)}
     */
    private static void dumpAST(Node node, Writer writer, int indent)
            throws IOException {
        writer.write(getIndentString(indent));
        writer.write("AST" + node.toString());

        if (DUMP_IMAGE)
            try {
                Method getImage = node.getClass().getMethod("getImage", new Class[0]);
                writer.write(" image=\"" + getImage.invoke(node, new Object[0]) + "\"");
            } catch (NoSuchMethodException e1) {
            } catch (IllegalAccessException e2) {
            } catch (InvocationTargetException e3) {
            }
        
        if (DUMP_SPECIALS)
            try {
                Field specials = node.getClass().getField("specials");
                Object specialsValue = specials.get(node);
                if (specialsValue != null)
                    writer.write(" specials=\"" + specialsValue + "\"");
            } catch (NoSuchFieldException e1) {
            } catch (IllegalAccessException e2) {
            }
        
        if (DUMP_TYPE) {
            Type type = Type.getType(node);
            if (type != null)
                writer.write(" type=\"" + type + "\"");
        }

        writer.write('\n');

        for (int i=0; i<node.jjtGetNumChildren(); i++)
            dumpAST(node.jjtGetChild(i), writer, indent + 2);
    }
}
