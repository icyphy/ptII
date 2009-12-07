/**
 * 
 */
package ptolemy.domains.interfaces;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import ptolemy.actor.lib.Expression;
import ptolemy.data.expr.ParseTreeDumper;
import ptolemy.data.expr.PtParser;

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
        
        if (result.equals("")) {
            // could not get proof
            _setSVG("orange");
        } else if (result.charAt(0) == 's') {
            // sat
            _setSVG("blue");
        } else if (result.charAt(0) == 'u') {
            // unsat
            _setSVG("red");
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
            + "<rect width=\"200\" height=\"50\" style=\"fill:"
            + color + "\"/>\n" + text + "\n</svg>"; 
        _attachText("_iconDescription", svg);
    }

}
