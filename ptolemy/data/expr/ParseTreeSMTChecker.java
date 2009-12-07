/**
 * 
 */
package ptolemy.data.expr;

import java.io.PrintStream;
import java.util.HashMap;

import ptolemy.data.smtsolver.SMTSolver;
import ptolemy.kernel.util.IllegalActionException;

/**
 * @author blickly
 *
 */
public class ParseTreeSMTChecker extends AbstractParseTreeVisitor {
    
    public void checkParseTree(ASTPtRootNode root) {
        _smtDefines = new HashMap<String, String>();
        _smtFormula = "(assert ";
        try {
            root.visit(this);
        } catch (IllegalActionException ex) {
            _stream.println(ex);
            ex.printStackTrace(_stream);
        }
        _smtFormula += ")\n";
        _stream.println("SMT Checker formula:");
        String yicesIn = "";
        for (String var : _smtDefines.keySet()) {
            yicesIn += "(define " + var + "::"
                + _smtDefines.get(var) + ")\n";   
        }
        yicesIn += _smtFormula
             + "(set-evidence! true)\n"
             + "(check)\n";
        _stream.println(yicesIn);
        _stream.println("Solver result: " + _solver.check(yicesIn));
        
        yicesIn = "(define foo::int)" +
        "\n(assert (= (mod foo 7 ) 4))" +
        "\n(set-evidence! true)" +
        "\n(check)";
        _stream.println("Solver result: " + _solver.check(yicesIn));
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    public void visitLeafNode(ASTPtLeafNode node) throws IllegalActionException {
        if (node.isIdentifier()) {
            _smtFormula += node.getName();
            _smtDefines.put(node.getName(), node.getType().toString());
        } else {
            String constName = node.toString();
            constName = constName.replaceAll(":null", "");
            constName = constName.replaceAll(".*:", "");
            _smtFormula += constName;
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
        _smtFormula += "(" + op + " ";
        _visitChildren(node);
        _smtFormula += ")";
    }

    public void visitProductNode(ASTPtProductNode node)
           throws IllegalActionException {
        String op = node.getLexicalTokenList().get(0).toString();
        if (op.equals("%")) {
            op = "mod";
        }
        _smtFormula += "(" + op + " ";
        _visitChildren(node);
        _smtFormula += ")";
    }

    public void visitRelationalNode(ASTPtRelationalNode node)
            throws IllegalActionException {
        String op = node.getOperator().toString();
        if (op.equals("==")) {
            op = "=";
        } else if (op.equals("!=")) {
            op = "/=";
        }
        _smtFormula += "(" + op + " ";
        _visitChildren(node);
        _smtFormula += ")";
    }

    public void visitSumNode(ASTPtSumNode node) throws IllegalActionException {
        _smtFormula += "(" + node.getLexicalTokenList().get(0) + " ";
        _visitChildren(node);
        _smtFormula += ")";
    }
    
    public void visitUnaryNode(ASTPtUnaryNode node)
            throws IllegalActionException {
        String op = node.getOperator().toString();
        if (op.equals("!")) {
            op = "not";
        }
        _smtFormula += "(" + op + " ";
        _visitChildren(node);
        _smtFormula += ")";
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
                _smtFormula += " ";
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////
    private HashMap<String, String> _smtDefines;
    private String _smtFormula;

    private PrintStream _stream = System.out;
    private SMTSolver _solver = new SMTSolver();

}
