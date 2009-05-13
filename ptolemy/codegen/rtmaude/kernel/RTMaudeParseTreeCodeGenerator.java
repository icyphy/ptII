package ptolemy.codegen.rtmaude.kernel;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ptolemy.codegen.kernel.ParseTreeCodeGenerator;
import ptolemy.data.ArrayToken;
import ptolemy.data.BitwiseOperationToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.FunctionToken;
import ptolemy.data.LongToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.RecordToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.ASTPtArrayConstructNode;
import ptolemy.data.expr.ASTPtBitwiseNode;
import ptolemy.data.expr.ASTPtFunctionApplicationNode;
import ptolemy.data.expr.ASTPtFunctionDefinitionNode;
import ptolemy.data.expr.ASTPtFunctionalIfNode;
import ptolemy.data.expr.ASTPtLeafNode;
import ptolemy.data.expr.ASTPtLogicalNode;
import ptolemy.data.expr.ASTPtMatrixConstructNode;
import ptolemy.data.expr.ASTPtMethodCallNode;
import ptolemy.data.expr.ASTPtPowerNode;
import ptolemy.data.expr.ASTPtProductNode;
import ptolemy.data.expr.ASTPtRecordConstructNode;
import ptolemy.data.expr.ASTPtRelationalNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ASTPtShiftNode;
import ptolemy.data.expr.ASTPtSumNode;
import ptolemy.data.expr.ASTPtUnaryNode;
import ptolemy.data.expr.AbstractParseTreeVisitor;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.StringUtilities;

public class RTMaudeParseTreeCodeGenerator extends AbstractParseTreeVisitor 
    implements ParseTreeCodeGenerator {
    
    public RTMaudeParseTreeCodeGenerator() {
        super();
    }

    public ptolemy.data.Token evaluateParseTree(ASTPtRootNode node,
            ParserScope scope) throws IllegalActionException {
        
        idTable = new HashMap<String,Set<String>>();
        idTable.put("variable", new HashSet<String>());
        idTable.put("port", new HashSet<String>());

        StringWriter writer = new StringWriter();

        _writer = new PrintWriter(writer);
        node.visit(this);
        this.result = writer.toString();
        return null;
    }

    public String generateFireCode() {
        return result;
    }
    
    public Map<String,Set<String>> getIdTable() {
        return idTable;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    public void visitLeafNode(ASTPtLeafNode node) throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            ptolemy.data.Token tok = node.getToken();
            String res = node.getToken().toString();
            
            //FIXME: complex value (e.g. 0.0 + 3.2i)
            if ( tok instanceof StringToken ) {
                _writer.print("# " + escapeForTargetLanguage(res));
            }
            else {
                _writer.print("# " + res);
            }
        } else {
            _writer.print(_transformLeaf(node.getName()));        // when variable identifier
        }
    }
    
    public void visitArrayConstructNode(ASTPtArrayConstructNode node) throws IllegalActionException {
        _writer.print("{");
        _printChildrenSeparated(node, ", ");
        _writer.print("}");
    }
     
    public void visitLogicalNode(ASTPtLogicalNode node) throws IllegalActionException {
        _writer.print("(");
        _printChildrenSeparated(node, node.getOperator().image);
        _writer.print(")");
    }
    
    public void visitBitwiseNode(ASTPtBitwiseNode node) throws IllegalActionException {
        _writer.print("(");
        _printChildrenSeparated(node, node.getOperator().image);
        _writer.print(")");
    }
    
    public void visitPowerNode(ASTPtPowerNode node) throws IllegalActionException {
        _writer.print("(");
        _printChildrenSeparated(node, "^");
        _writer.print(")");
    }
    
    public void visitProductNode(ASTPtProductNode node) throws IllegalActionException {
        _writer.print("(");
        _printChildrenSeparated(node, node.getLexicalTokenList());
        _writer.print(")");
    }
    
    public void visitRelationalNode(ASTPtRelationalNode node) throws IllegalActionException {
        _writer.print("(");
        _printChildrenSeparated(node, node.getOperator().image);
        _writer.print(")");
    }

    public void visitShiftNode(ASTPtShiftNode node) throws IllegalActionException {
        _writer.print("(");
        _printChildrenSeparated(node, node.getOperator().image);
        _writer.print(")");
    }

    public void visitSumNode(ASTPtSumNode node) throws IllegalActionException {
        _writer.print("(");
        _printChildrenSeparated(node, node.getLexicalTokenList());
        _writer.print(")");
    }

    public void visitUnaryNode(ASTPtUnaryNode node) throws IllegalActionException {
        _writer.print("(");
        if (node.isMinus()) {
            _writer.print("- ");
        } else if (node.isNot()) {
            _writer.print("! ");
        } else {
            _writer.print("~ ");
        }
        _printChild(node, 0);
        _writer.print(")");
    }

    public void visitFunctionalIfNode(ASTPtFunctionalIfNode node) throws IllegalActionException {
        _writer.print("(");
        _printChild(node, 0);
        _writer.print(" ? ");
        _printChild(node, 1);
        _writer.print(" : ");
        _printChild(node, 2);
        _writer.print(")");
    }
    
    public void visitFunctionApplicationNode(ASTPtFunctionApplicationNode node)
            throws IllegalActionException {
        _writer.print("(");
        _printChild(node, 0);
        _writer.print(" $ (");
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            _printChild(node, i);
            if ( i < node.jjtGetNumChildren() - 1)
                _writer.print(", ");
        }
        _writer.print("))");
    }

    public void visitFunctionDefinitionNode(ASTPtFunctionDefinitionNode node)
            throws IllegalActionException {
        _writer.print("(function(");

        List args = node.getArgumentNameList();
        int n = args.size();

        // Notice : type constraints are omitted.
        for (int i = 0; i < n; i++) {
            if (i > 0) _writer.print(", ");
            _writer.print("'" + (String)args.get(i) );
        }

        _writer.print(") ");
        node.getExpressionTree().visit(this);
        _writer.print(")");
    }

    public void visitMatrixConstructNode(ASTPtMatrixConstructNode node) throws IllegalActionException {
        int n = 0;
        int rowCount = node.getRowCount();
        int columnCount = node.getColumnCount();
        
        _writer.print("[");
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                _printChild(node, n++);
                if (j < columnCount - 1) _writer.print(", ");
            }
            if (i < rowCount - 1) _writer.print("; ");
        }
        _writer.print("]");
    }

    public void visitMethodCallNode(ASTPtMethodCallNode node) throws IllegalActionException {
        _writer.print("(");
        _printChild(node, 0);
        _writer.print(" .. ");                           // .. instead of .
        _writer.print("'" + node.getMethodName());      //
        _writer.print("(");
        if (node.jjtGetNumChildren() > 1) {
            for (int i = 1; i < node.jjtGetNumChildren(); i++) {
                if (i > 1) _writer.print(", ");
                _printChild(node, i);
            }
        } else {
            _writer.print("nil");
        }
        _writer.print(")");
        _writer.print(")");
    }


    public void visitRecordConstructNode(ASTPtRecordConstructNode node)
            throws IllegalActionException {
        Iterator names = node.getFieldNames().iterator();
        _writer.print("{");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            if (i > 0) _writer.print(", ");
            _writer.print("(" + "'" + names.next().toString());          // 
            _writer.print(" <- ");      // <- instead =
            _printChild(node, i);
            _writer.print(")");
        }
        _writer.print("}");
    }
    
    private void _printChild(ASTPtRootNode node, int index)
            throws IllegalActionException {
        ((ASTPtRootNode) node.jjtGetChild(index)).visit(this);
    }
    
    private void _printChildrenSeparated(ASTPtRootNode node, List separatorList)
            throws IllegalActionException {
        Iterator separators = separatorList.iterator();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            if (i > 0) _writer.print(" " + _transformOp(((Token) separators.next()).image) + " ");
            _printChild(node, i);
        }
    }
    
    private void _printChildrenSeparated(ASTPtRootNode node, String string)
            throws IllegalActionException {
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            if (i > 0) _writer.print(" " + _transformOp(string) + " ");
            _printChild(node, i);
        }
    }
    
    private static String _transformOp(String op) {
        if (op.equals("<")) return "lessThan";
        else if (op.equals(">")) return "greaterThan";
        else if (op.equals("==")) return "equals";
        else return op;
        // && ||
        // & | # 
        //<< >> >>>
    }
    
    private String _transformLeaf(String id) {        
        Matcher m = Pattern.compile("(.*)_isPresent").matcher(id);
        if (m.matches()) {
            String pid = m.group(1);
            idTable.get("port").add(pid);
            return "isPresent(" + "'" + pid + ")";
        }
        else {
            if (id.equals("Infinity"))
                return id;
            else {
                idTable.get("variable").add(id);
                return "'" + id;
            }
        }
    }

    public String escapeForTargetLanguage(String string) {
        string = StringUtilities.substitute(string, "\\", "\\\\");
        string = StringUtilities.substitute(string, "\"", "\\\"");
        string = StringUtilities.substitute(string, "\n", "\\n");
        return string;
    }
    
    protected PrintWriter _writer;
    private Map<String,Set<String>> idTable;
    private String result = null;
}
