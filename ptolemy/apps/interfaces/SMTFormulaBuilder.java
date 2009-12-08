/** A parse tree visitor that produces an SMT formula from a Ptolemy AST.
 * 
 */
package ptolemy.apps.interfaces;

import ptolemy.data.expr.ASTPtLeafNode;
import ptolemy.data.expr.ASTPtLogicalNode;
import ptolemy.data.expr.ASTPtProductNode;
import ptolemy.data.expr.ASTPtRelationalNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ASTPtSumNode;
import ptolemy.data.expr.ASTPtUnaryNode;
import ptolemy.data.expr.AbstractParseTreeVisitor;
import ptolemy.kernel.util.IllegalActionException;

/** A parse tree visitor that produces an SMT formula from a Ptolemy AST.
 * 
 *  This converts Ptolemy expressions into LISP-style expressions,
 *  represented as strings.  These can be later used in LISP-like
 *  languages, such as the Yices SMT solver's input language.
 *  
 *  Note: This class currently ignores the types in the Ptolemy
 *  expression.  They may be needed in some applications.
 * 
 *  @author Ben Lickly
 *
 */
public class SMTFormulaBuilder extends AbstractParseTreeVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Produce a LISP-like expression from a Ptolemy AST.
     *  
     *  @param root The root node of the Ptolemy expression.
     *  @return A string representation of the expression.
     */
    public String parseTreeToSMTFormula(final ASTPtRootNode root) {
        _smtFormula = new StringBuffer();

        try {
            root.visit(this);
        } catch (final IllegalActionException ex) {
            System.err.println(ex);
            ex.printStackTrace(System.err);
        }

        return _smtFormula.toString();
    }

    @Override
    public void visitLeafNode(ASTPtLeafNode node) throws IllegalActionException {
        if (node.isIdentifier()) {

            _smtFormula.append(node.getName());
            /* FIXME: This is where we will find the types once we implement
             * type checking on this AST */
            //_smtDefines.put(node.getName(), node.getType().toString());
        } else {
            String constName = node.toString();
            constName = constName.replaceAll(":null", "");
            constName = constName.replaceAll(".*:", "");
            _smtFormula.append(constName);
        }
    }

    @Override
    public void visitLogicalNode(ASTPtLogicalNode node)
            throws IllegalActionException {
        String op = node.getOperator().toString();
        if (op.equals("&&")) {
            op = "and";
        } else if (op.equals("||")) {
            op = "or";
        }
        _smtFormula.append("(" + op + " ");
        _visitChildren(node);
        _smtFormula.append(")");
    }

    @Override
    public void visitProductNode(ASTPtProductNode node)
            throws IllegalActionException {
        String op = node.getLexicalTokenList().get(0).toString();
        if (op.equals("%")) {
            op = "mod";
        }
        _smtFormula.append("(" + op + " ");
        _visitChildren(node);
        _smtFormula.append(")");
    }

    @Override
    public void visitRelationalNode(ASTPtRelationalNode node)
            throws IllegalActionException {
        String op = node.getOperator().toString();
        if (op.equals("==")) {
            op = "=";
        } else if (op.equals("!=")) {
            op = "/=";
        }
        _smtFormula.append("(" + op + " ");
        _visitChildren(node);
        _smtFormula.append(")");
    }

    @Override
    public void visitSumNode(ASTPtSumNode node) throws IllegalActionException {
        _smtFormula.append("(" + node.getLexicalTokenList().get(0) + " ");
        _visitChildren(node);
        _smtFormula.append(")");
    }

    @Override
    public void visitUnaryNode(ASTPtUnaryNode node)
            throws IllegalActionException {
        String op = node.getOperator().toString();
        if (op.equals("!")) {
            op = "not";
        }
        _smtFormula.append("(" + op + " ");
        _visitChildren(node);
        _smtFormula.append(")");
    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected methods                    ////
    /** Recurse into the children of the given node.
     *  
     *  @param node The node to be explored.
     *  @throws IllegalActionException If there is a problem displaying
     *   the children.
     */
    protected void _visitChildren(ASTPtRootNode node)
            throws IllegalActionException {
        if (node.jjtGetNumChildren() > 0) {
            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                final ASTPtRootNode child = (ASTPtRootNode) node.jjtGetChild(i);
                child.visit(this);
                _smtFormula.append(" ");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////
    /** The intermediate state of the LISP-expression. 
     */
    private StringBuffer _smtFormula;

}
