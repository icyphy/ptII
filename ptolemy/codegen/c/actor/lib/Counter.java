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
public class Counter extends CCodeGeneratorHelper {

        /**
         * @param component
         */
    public Counter(ptolemy.actor.lib.Counter actor) {
        super(actor);
    }

    public void  generateFireCode(StringBuffer stream)
            throws IllegalActionException {

            ptolemy.actor.lib.Sequence actor =
                    (ptolemy.actor.lib.Sequence)getComponent();

            StringBuffer tmpStream = new StringBuffer();

            tmpStream.append(
                              "if ($val(increment)) {\n"
                + "    $val(output)++;\n"
                + "} else if ($val(decrement)) {\n"
                                + "    $val(output)--;\n"
                + "}\n");


            _codeBlock = tmpStream.toString();
        stream.append(processCode(_codeBlock));
    }



///////////////////////////////////////////////////////////////////
////                     protected variables                   ////

    protected String _codeBlock;

}
