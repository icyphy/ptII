package ptolemy.apps.ptalon.model;

import java.util.Stack;

import ptolemy.data.Token;
import antlr.collections.AST;

public class LoopedAstReference {

    public LoopedAstReference(AST ast, Stack<Token> forTokens) {
        _forTokens = new Stack<Token>();
        for (int i = 0; i < forTokens.size(); i++) {
            _forTokens.push(forTokens.elementAt(i));
        }
        _ast = ast;
    }

    public boolean equals(Object obj) {
        if (obj instanceof LoopedAstReference) {
            LoopedAstReference ref = (LoopedAstReference) obj;
            if (ref._ast != _ast) {
                return false;
            }
            if (ref._forTokens.size() != _forTokens.size()) {
                return false;
            }
            for (int i = 0; i < _forTokens.size(); i++) {
                if (!ref._forTokens.elementAt(i)
                        .equals(_forTokens.elementAt(i))) {
                    return false;
                }
            }
            return true;
        }
        return super.equals(obj);
    }

    public int hashCode() {
        int code = _ast.hashCode();
        for (int i = 0; i < _forTokens.size(); i++) {
            code ^= _forTokens.elementAt(i).hashCode();
        }
        return code;
    }

    private Stack<Token> _forTokens;

    private AST _ast;
}
