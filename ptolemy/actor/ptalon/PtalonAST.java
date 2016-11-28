/* FIXME comment

 Copyright (c) 2006-2016 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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
package ptolemy.actor.ptalon;

import java.io.IOException;
import java.io.Writer;

import antlr.CommonAST;
import antlr.Token;
import antlr.collections.AST;
import ptolemy.util.StringUtilities;

/**
 This is just like CommonAST, except it allows XML serialization to be
 parameterized by a depth.

 @author Adam Cataldo, Elaine Cheong
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (acataldo)
 @Pt.AcceptedRating Red (acataldo)
 */
@SuppressWarnings("serial")
public class PtalonAST extends CommonAST {

    /** Call the default constructor.
     */
    public PtalonAST() {
        super();
    }

    /** Call the default constructor.
     *  @param tok The token for this node.
     */
    public PtalonAST(Token tok) {
        super(tok);
    }

    /**
     * @return An XML String version of this AST.
     *
     public String toString() {
     StringWriter writer = new StringWriter();
     try {
     xmlSerialize(writer, 0);
     } catch (IOException ex) {
     return "";
     }
     return writer.toString();
     }*/

    /** Generate the XML for this AST.
     *  @param out The writer to write to.
     *  @param depth The depth of this node.
     *  @exception IOException If there is any problem writing.
     */
    public void xmlSerialize(Writer out, int depth) throws IOException {
        for (AST node = this; node != null; node = node.getNextSibling()) {
            if (node.getFirstChild() == null) {
                // print guts (class name, attributes)
                out.write(_getIndentPrefix(depth));
                ((PtalonAST) node).xmlSerializeNode(out);
                out.write("\n");
            } else {
                out.write(_getIndentPrefix(depth));
                ((PtalonAST) node).xmlSerializeRootOpen(out);

                // print children
                ((PtalonAST) node.getFirstChild()).xmlSerialize(out, depth + 1);

                // print end tag
                out.write(_getIndentPrefix(depth));
                ((PtalonAST) node).xmlSerializeRootClose(out);
            }
        }
    }

    /** Return a number of spaces that is proportional to the argument.
     *  If the argument is negative or zero, return an empty string.
     *  @param level The level of indenting represented by the spaces.
     *  @return A string with zero or more spaces.
     */
    protected static String _getIndentPrefix(int level) {
        return StringUtilities.getIndentPrefix(level);
    }
}
