/* A helper class for ptolemy.actor.lib.Sequence

Copyright (c) 1997-2005 The Regents of the University of California.
All rights reserved.
Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

PT_COPYRIGHT_VERSION_2
COPYRIGHTENDKEY

*/
package ptolemy.codegen.c.actor.lib;

import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Sequence
/**
   A helper class for ptolemy.actor.lib.Sequence

   @author Man-Kit (Jackie) Leung, Gang Zhou
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (eal)
   @Pt.AcceptedRating Red (eal)
*/
public class Sequence extends CCodeGeneratorHelper {

    /** FIXME
     *
     */
    public Sequence(ptolemy.actor.lib.Sequence actor) {
        super(actor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void  generateFireCode(StringBuffer stream)
            throws IllegalActionException {

        ptolemy.actor.lib.Sequence actor =
            (ptolemy.actor.lib.Sequence)getComponent();

        StringBuffer tmpStream = new StringBuffer();

        if (actor.enable.getWidth() == 0) {
            tmpStream.append(
                      "if (currentIndex < $size(values)) {\n"
                    + "    $ref(output) = $ref(values, currentIndex);\n"
                    + "    outputProduced = 1;\n"
                    + "}\n");
        } else {
            tmpStream.append(
                      "if ($ref(enable) \n"
                    + "        && currentIndex < $size(values)) {\n"
                    + "    $ref(output) = $ref(values, currentIndex);\n"
                    + "    outputProduced = 1;\n"
                    + "}\n");
        }

        tmpStream.append(
                  "if (outputProduced) {\n"
                + "    outputProduced = 0;\n"
                + "    currentIndex += 1;\n"
                + "    if (currentIndex >= $size(values)) {\n"
                + "        if ($val(repeat)) {\n"
                + "           currentIndex = 0;\n"
                + "        } else {\n"
                + "           /*To prevent overflow...*/\n"
                + "           currentIndex = $size(values);\n"
                + "        }\n"
                + "    }\n"
                + "}\n");


        _codeBlock = tmpStream.toString();
        stream.append(processCode(_codeBlock));
    }

    public String generateInitializeCode()
            throws IllegalActionException {
        return processCode(_initBlock);
}


    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    protected String _codeBlock;

    // FIXME: This is not going to work since currentIndex
    // and outputProduced are not defined in java as variables.
    // E.g., $ref(ouput, currentIndex) does not make sense to java.
    protected String _initBlock =
              "int currentIndex = 0;\n"
            + "int outputProduced = 0;\n";
}
