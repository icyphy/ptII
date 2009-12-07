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
        _setSVG("white");
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
        String result = ptsc.checkParseTree(_parseTree);
        
        System.err.println("Result: " + result);
        if (!result.equals("")) {
            if (result.substring(0, 3).equals("sat")) {
                _setSVG("green");
            } else if (result.substring(0, 3).equals("uns")) {
                _setSVG("red");
            }
            
        }
    }
    
    private void _setSVG(String color) {
        String text = expression.getExpression();
        if (!text.equals("")) {
            text = "<text id=\"TextElement\" x=\"20\" y=\"20\" "
                + "style=\"font-size:14; font-family:SansSerif\">"
                + text + "</text>"; 
        }
        String svg = "<svg>\n" 
            + "<rect width=\"100\" height=\"50\" style=\"fill:"
            + color + "\"/>\n" + text + "\n</svg>"; 
        _attachText("_iconDescription", svg);
    }

}
