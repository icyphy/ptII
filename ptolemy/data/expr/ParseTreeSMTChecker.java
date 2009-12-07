/**
 * 
 */
package ptolemy.data.expr;

import java.io.PrintStream;

import ptolemy.data.smtsolver.SMTSolver;
import ptolemy.kernel.util.IllegalActionException;

/**
 * @author blickly
 *
 */
public class ParseTreeSMTChecker extends AbstractParseTreeVisitor {
    
    public void checkParseTree(ASTPtRootNode root) {
        _smtDefines = "";
        _smtFormula = "(assert ";
        try {
            root.visit(this);
        } catch (IllegalActionException ex) {
            _stream.println(ex);
            ex.printStackTrace(_stream);
        }
        _smtFormula += ")\n";
        _stream.println("SMT Checker formula:");
        String yicesIn = _smtDefines + _smtFormula
             + "(set-evidence! true)\n"
             + "(check)\n";
        _stream.println(yicesIn);
        _stream.println("Solver result: " + _solver.check(yicesIn));
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    public void visitLeafNode(ASTPtLeafNode node) throws IllegalActionException {
        if (node.isIdentifier()) {
            _smtFormula += node.getName();
            _smtDefines += "(define " + node.getName() + "::"
                + node.getType() + ")\n";
        } else {
            String constName = node.toString();
            constName = constName.replaceAll(":null", "");
            constName = constName.replaceAll(".*:", "");
            _smtFormula += constName;
        }
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
        _smtFormula += "(+ ";
        _visitChildren(node);
        _smtFormula += ")";
    }

    /** Display the given node with the current prefix, recursing into
     *  the children of the node.
     *  @param node The node to be displayed.
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
    private String _smtDefines;
    private String _smtFormula;

    private PrintStream _stream = System.out;
    private SMTSolver _solver = new SMTSolver();

}
