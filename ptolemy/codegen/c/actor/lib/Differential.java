/*
 * Created on Feb 21, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ptolemy.codegen.c.actor.lib;

import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 * @author Man-Kit Leung
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Differential extends CCodeGeneratorHelper {

	/**
	 * @param component
	 */
    public Differential(ptolemy.actor.lib.Differential actor) {
        super(actor);
    }

    public void  generateFireCode(StringBuffer stream) 
            throws IllegalActionException {

    	ptolemy.actor.lib.Sequence actor = 
    		(ptolemy.actor.lib.Sequence)getComponent();

    	StringBuffer tmpStream = new StringBuffer();    	
    	
    	tmpStream.append("$val(output) = $val(input) - previousInput;\n");
        
    	_codeBlock = tmpStream.toString();
        stream.append(processCode(_codeBlock));
    }

    public String generateInitializeCode()
            throws IllegalActionException {
    	return processCode(_initBlock);
    }


///////////////////////////////////////////////////////////////////
////                     protected variables                   ////

    protected String _codeBlock;
    
    protected String _initBlock = 
    	    "int previousInput = 0;\n";    
}
