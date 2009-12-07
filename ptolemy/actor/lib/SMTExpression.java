/**
 * 
 */
package ptolemy.actor.lib;

import java.io.IOException;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import ptolemy.data.expr.ParseTreeDumper;
import ptolemy.data.expr.ParseTreeSMTChecker;
import ptolemy.data.expr.PtParser;
import ptolemy.data.smtsolver.SMTSolver;

/**
 * @author blickly
 *
 */
public class SMTExpression extends Expression {
    
    /**
     * @param container
     * @param name
     * @throws NameDuplicationException
     * @throws IllegalActionException
     */
    public SMTExpression(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        // TODO Auto-generated constructor stub
    }
    
    public void initialize() throws IllegalActionException {
        super.initialize();
        
        // Make sure we have the parse tree (code from fire of Expression)
        if (_parseTree == null) {
            PtParser parser = new PtParser();
            _parseTree = parser.generateParseTree(expression
                    .getExpression());
        }
        
        ParseTreeDumper ptd = new ParseTreeDumper();
        ptd.displayParseTree(_parseTree);

        ParseTreeSMTChecker ptsc = new ParseTreeSMTChecker();
        ptsc.checkParseTree(_parseTree);
    }

}
