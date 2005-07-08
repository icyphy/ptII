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

 @author Man-Kit Leung, Gang Zhou
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class Sequence extends CCodeGeneratorHelper {
    /**
     * Constructor method for the Sequence helper
     * @param actor the associated actor
     */
    public Sequence(ptolemy.actor.lib.Sequence actor) {
        super(actor);
    }

    /**
     * Generate fire code
     * The method reads in codeBlock1 and puts into the
     * given stream buffer
     * @param stream the given buffer to append the code to
     */
    public void generateFireCode(StringBuffer stream)
            throws IllegalActionException {
        ptolemy.actor.lib.Sequence actor = (ptolemy.actor.lib.Sequence) getComponent();

        CodeStream tmpStream = new CodeStream(this);

        if (actor.enable.getWidth() == 0) {
            tmpStream.appendCodeBlock("codeBlock1");
        } else {
            tmpStream.appendCodeBlock("codeBlock2");
        }

        tmpStream.appendCodeBlock("codeBlock3");

        stream.append(processCode(tmpStream.toString()));
    }
}
