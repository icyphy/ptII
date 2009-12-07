/** Parse tree visitor that produces an SMT formula from a Ptolemy AST.
 * 
 */
package ptolemy.domains.interfaces;

import java.io.PrintStream;
import java.util.HashMap;

import ptolemy.data.expr.ASTPtLeafNode;
import ptolemy.data.expr.ASTPtLogicalNode;
import ptolemy.data.expr.ASTPtProductNode;
import ptolemy.data.expr.ASTPtRelationalNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ASTPtSumNode;
import ptolemy.data.expr.ASTPtUnaryNode;
import ptolemy.data.expr.AbstractParseTreeVisitor;
import ptolemy.kernel.util.IllegalActionException;

/**
 * @author blickly
 *
 */
public class SMTFormulaBuilder extends AbstractParseTreeVisitor {
    
    public String parseTreeToSMTFormula(ASTPtRootNode root) {
        _smtDefines = new HashMap<String, String>();
        _smtFormula = new StringBuffer("(assert ");
        
        try {
            root.visit(this);
        } catch (IllegalActionException ex) {
            _stream.println(ex);
            ex.printStackTrace(_stream);
        }
        _smtFormula.append(")\n");
        
        StringBuffer defines = new StringBuffer();
        for (String var : _smtDefines.keySet()) {
            defines.append("(define " + var + "::"
                + _smtDefines.get(var) + ")\n");   
        }
        
        return defines.toString() + _smtFormula.toString();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    public void visitLeafNode(ASTPtLeafNode node) throws IllegalActionException {
        if (node.isIdentifier()) {
            _smtFormula.append(node.getName());
            _smtDefines.put(node.getName(), "int");//node.getType().toString());
        } else {
            String constName = node.toString();
            constName = constName.replaceAll(":null", "");
            constName = constName.replaceAll(".*:", "");
            _smtFormula.append(constName);
        }
    }
    
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

    public void visitSumNode(ASTPtSumNode node) throws IllegalActionException {
        _smtFormula.append("(" + node.getLexicalTokenList().get(0) + " ");
        _visitChildren(node);
        _smtFormula.append(")");
    }
    
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
     *  @param node The node to be explored.
     *  @exception If there is a problem displaying the children.
     */
    protected void _visitChildren(ASTPtRootNode node)
            throws IllegalActionException {
        if (node.jjtGetNumChildren() > 0) {
              for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                ASTPtRootNode child = (ASTPtRootNode) node.jjtGetChild(i);
                child.visit(this);
                _smtFormula.append(" ");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////
    private HashMap<String, String> _smtDefines;
    private StringBuffer _smtFormula;

    private PrintStream _stream = System.out;

}
