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
public class Average extends CCodeGeneratorHelper {

        /**
         * @param component
         */
    public Average(ptolemy.actor.lib.Average actor) {
        super(actor);
    }

    public void  generateFireCode(StringBuffer stream)
            throws IllegalActionException {

            ptolemy.actor.lib.Sequence actor =
                    (ptolemy.actor.lib.Sequence)getComponent();

            StringBuffer tmpStream = new StringBuffer();

            tmpStream.append(
                              "if ($val(reset)) {\n"
                            + "    sum = 0;\n"
                                + "    count = 0;\n"
                + "} else {\n"
                                + "    sum += $val(input);\n"
                + "    count++;\n"
                                + "    $val(output) = sum / count;\n"
                + "}\n");


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
          "int sum = 0;\n"
        + "int count = 0;\n";
}
