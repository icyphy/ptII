/* A code generation helper class for actor.lib.Interpolator
 @Copyright (c) 2006-2009 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN \"AS IS\" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.codegen.c.actor.lib;

import java.util.LinkedList;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.data.ArrayToken;
import ptolemy.kernel.util.IllegalActionException;

/**
 A code generation helper class for ptolemy.actor.lib.Interpolator.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class Interpolator extends CCodeGeneratorHelper {

    /**
     * Construct a Interpolator helper.
     * @param actor The associated actor.
     */
    public Interpolator(ptolemy.actor.lib.Interpolator actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * Read the <code>fireBlock</code> from Interpolator.c,
     * replace macros with their values and return the processed code string.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    protected String _generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super._generateFireCode());
        LinkedList args = new LinkedList();
        ptolemy.actor.lib.Interpolator actor = (ptolemy.actor.lib.Interpolator) getComponent();
        ArrayToken indexesValue = (ArrayToken) actor.indexes.getToken();
        int length = indexesValue.length();
        args.add(length);
        args.add(indexesValue.getElement(length - 1));

        code.append(_generateBlockCode("fireBlock", args));
        return code.toString();
    }
}
