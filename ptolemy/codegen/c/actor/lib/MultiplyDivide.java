/* A helper class for ptolemy.actor.lib.MultiplyDivide

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
//// MultiplyDivide

/**
 A helper class for ptolemy.actor.lib.MultiplyDivide.

 @author Man-Kit (Jackie) Leung, Gang Zhou
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class MultiplyDivide extends CCodeGeneratorHelper {
    /**
     * Constructor method for the MultiplyDivide helper.
     * @param actor the associated actor
     */
    public MultiplyDivide(ptolemy.actor.lib.MultiplyDivide actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method generate code that loops through each
     * INPUT [multi-ports] and combine (add or substract) them.
     * The result code is put into the given stream buffer.
     * @param stream the given buffer to append the code to.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public void generateFireCode(StringBuffer stream)
            throws IllegalActionException {
        ptolemy.actor.lib.MultiplyDivide actor = 
            (ptolemy.actor.lib.MultiplyDivide) getComponent();
        StringBuffer tmpStream = new StringBuffer();
        tmpStream.append("$ref(output) = ");

        for (int i = 0; i < actor.multiply.getWidth(); i++) {
            tmpStream.append("$ref(multiply#" + i + ")");

            if (i < (actor.multiply.getWidth() - 1)) {
                tmpStream.append(" * ");
            } else if (actor.divide.getWidth() > 0) {
                tmpStream.append(" / ");
            }
        }

        // assume numerator of 1, if no input is connected to
        // the multiply ports
        if (actor.multiply.getWidth() == 0) {
            tmpStream.append(" 1.0 / ");
        }

        for (int i = 0; i < actor.divide.getWidth(); i++) {
            tmpStream.append("$ref(divide#" + i + ")");

            if (i < (actor.divide.getWidth() - 1)) {
                tmpStream.append(" / ");
            }
        }
        tmpStream.append(";\n");
        stream.append(processCode(tmpStream.toString()));
    }
}
