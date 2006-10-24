package ptolemy.actor.ptalon;

import java.io.IOException;
import java.io.Writer;

import ptolemy.util.StringUtilities;
import antlr.CommonAST;
import antlr.Token;
import antlr.collections.AST;

/**
 * This is just like CommonAST, except it allows
 * xml serialization to be parameterized by a depth.
 * @author acataldo
 *
 */
public class PtalonAST extends CommonAST {

    /**
     * Call the default constructor.
     *
     */
    public PtalonAST() {
        super();
    }

    /**
     * Call the default constructor.
     * @param tok The token for this node.
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
     } catch (IOException e) {
     return "";
     }
     return writer.toString();
     }*/

    /**
     * Generate the XML for this AST.
     * @param out The writer to write to.
     * @param depth The depth of this node.
     * @throws IOException If there is any problem writing.
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
