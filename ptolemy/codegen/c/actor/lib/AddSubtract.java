/* A helper class for ptolemy.actor.lib.AddSubtract

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
//// AddSubtract

/**
   A helper class for ptolemy.actor.lib.AddSubtract

   @author Man-Kit (Jackie) Leung, Gang Zhou
   @version $Id$
   @since Ptolemy II 5.0
   @Pt.ProposedRating Red (eal)
   @Pt.AcceptedRating Red (eal)
*/
public class AddSubtract extends CCodeGeneratorHelper {
    /**
     * Constructor method for the AddSubtract helper
     * @param actor the associated actor
     */
    public AddSubtract(ptolemy.actor.lib.AddSubtract actor) {
        super(actor);
    }

    /**
     * Generate fire code
     * The method generate code that loops through each
     * INPUT [multi-ports] and combine (add or substract) them.
     * The result code is put into the given stream buffer
     * @param stream the given buffer to append the code to
     */
    public void generateFireCode(StringBuffer stream)
            throws IllegalActionException {
        ptolemy.actor.lib.AddSubtract actor = (ptolemy.actor.lib.AddSubtract) getComponent();
        StringBuffer tmpStream = new StringBuffer();
        tmpStream.append("$ref(output) = ");

        for (int i = 0; i < actor.plus.getWidth(); i++) {
            tmpStream.append("$ref(plus#" + i + ")");

            if (i < (actor.plus.getWidth() - 1)) {
                tmpStream.append(" + ");
            } else if (actor.minus.getWidth() > 0) {
                tmpStream.append(" - ");
            }
        }

        for (int i = 0; i < actor.minus.getWidth(); i++) {
            tmpStream.append("$ref(minus#" + i + ")");

            if (i < (actor.minus.getWidth() - 1)) {
                tmpStream.append(" - ");
            }
        }

        tmpStream.append(";\n");

        stream.append(processCode(tmpStream.toString()));
    }
}
