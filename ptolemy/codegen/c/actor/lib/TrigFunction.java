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

        ptolemy.actor.lib.AddSubtract actor =
            (ptolemy.actor.lib.AddSubtract)getComponent();
        StringBuffer tmpStream = new StringBuffer();
        tmpStream.append(
              "$val(output) = (!strcmp($ref(function), \"sin\")) ? sin($val(input)) : \n"
                + "           (!strcmp($ref(function), \"cos\")) ? cos($val(input)) : \n"
                + "           (!strcmp($ref(function), \"tan\")) ? tan($val(input)) : \n"
                + "           (!strcmp($ref(function), \"asin\")) ? asin($val(input)) : \n"
                + "           (!strcmp($ref(function), \"acos\")) ? acos($val(input)) : \n"
                + "           atan($val(input));\n");

        _codeBlock = tmpStream.toString();
        stream.append(processCode(_codeBlock));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variable                ////
    protected String _codeBlock;
}

