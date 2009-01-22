package ptolemy.verification.kernel.maude;

import ptolemy.data.expr.ASTPtRootNode;

import ptolemy.kernel.util.IllegalActionException;

public class RTMPtExp extends RTMFragment {
        
    public RTMPtExp(String exp) throws IllegalActionException {
        this(exp, false);
    }
    public RTMPtExp(ASTPtRootNode root) throws IllegalActionException {
        this(root, false);
    }
    public RTMPtExp(ASTPtRootNode root, boolean isTime) throws IllegalActionException {
        super(null);
        RTMExpTranslator rt = new RTMExpTranslator(isTime);
        this.frag = rt.translateParseTree(root);
    }
    public RTMPtExp(String exp, boolean isTime)  throws IllegalActionException {
        super(exp);
        RTMExpTranslator rt = new RTMExpTranslator(isTime);
        this.frag = rt.translateExpression(exp);
    }
    public String getValue() throws IllegalActionException {
    	String g = this.frag.trim();
    	if ( (g.startsWith("#r(") || g.startsWith("#f(") || g.startsWith("#b(")) && g.endsWith(")"))
    			return g.substring(3, g.length()-1);
    	else
    		throw new IllegalActionException("Not Value!");
    }
}
