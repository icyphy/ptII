/*
 * Created on Feb 21, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ptolemy.codegen.c.actor.lib;

import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

/**
 * @author Man-Kit Leung
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TrigFunction extends CCodeGeneratorHelper {

	/**
	 * @param component
	 */
    public TrigFunction(ptolemy.actor.lib.TrigFunction actor) {
        super(actor);
    }

    public void  generateFireCode(StringBuffer stream) 
        throws IllegalActionException {
    
        CodeStream tmpStream = new CodeStream(this);        

        tmpStream.append("codeBlock1");
        
        stream.append(processCode(tmpStream.toString()));
    }
}

