/* A code generation helper class for actor.lib.ArrayLevelCrossing

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

import java.util.Set;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.data.BooleanToken;
import ptolemy.kernel.util.IllegalActionException;

/**
 A code generation helper class for ptolemy.actor.lib.ArrayLevelCrossing.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Green (mankit)
 @Pt.AcceptedRating Green (cxh)
 */
public class ArrayLevelCrossing extends CCodeGeneratorHelper {

    /**
     * Construct an ArrayLevelCrossing helper.
     * @param actor The associated actor.
     */
    public ArrayLevelCrossing(ptolemy.actor.lib.ArrayLevelCrossing actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * Read the <code>fireBlock</code> from ArrayLevelCrossing.c,
     * replace macros with their values and append the processed code
     * block to the given code buffer.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    protected String _generateFireCode() throws IllegalActionException {
        super._generateFireCode();
        ptolemy.actor.lib.ArrayLevelCrossing actor = (ptolemy.actor.lib.ArrayLevelCrossing) getComponent();

        if (((BooleanToken) actor.forwards.getToken()).booleanValue()) {
            _codeStream.appendCodeBlock("forwardBlock");
        }

        String scaleValue = actor.scale.stringValue();
        String aboveValue = ((BooleanToken) actor.above.getToken())
                .booleanValue() ? "above" : "notAbove";

        if (scaleValue.equals("relative amplitude decibels")) {
            _codeStream.appendCodeBlock("amplitude_" + aboveValue);
        } else if (scaleValue.equals("relative power decibels")) {
            _codeStream.appendCodeBlock("power_" + aboveValue);
        } else if (scaleValue.equals("relative linear")) {
            _codeStream.appendCodeBlock("linear_" + aboveValue);
        }
        _codeStream.appendCodeBlock("findCrossing_" + aboveValue);

        return processCode(_codeStream.toString());
    }

    /**
     * Get the files needed by the code generated for the
     * ArrayLevelCrossing actor.
     * @return A set of Strings that are names of the header files
     *  needed by the code generated for the ArrayLevelCrossing actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = super.getHeaderFiles();
        files.add("<math.h>");
        files.add("<stdio.h>");
        return files;
    }
}
