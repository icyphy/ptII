/* A code generation helper class for actor.lib.ArrayPeakSearch
 @Copyright (c) 2007-2009 The Regents of the University of California.
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

import java.util.ArrayList;
import java.util.Set;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 A code generation helper class for ptolemy.actor.lib.ArrayPeakSearch.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class ArrayPeakSearch extends CCodeGeneratorHelper {

    /**
     * Constructor method for the ArrayPeakSearch helper.
     * @param actor The associated actor.
     */
    public ArrayPeakSearch(ptolemy.actor.lib.ArrayPeakSearch actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * Read the <code>fireBlock</code> from ArrayPeakSearch.c,
     * replace macros with their values and append the processed code
     * block to the given code buffer.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    protected String _generateFireCode() throws IllegalActionException {
        super._generateFireCode();
        ptolemy.actor.lib.ArrayPeakSearch actor = (ptolemy.actor.lib.ArrayPeakSearch) getComponent();

        String scale = actor.scale.stringValue();

        if (scale.equals("absolute")) {
            scale = "ABSOLUTE";
        } else if (scale.equals("relative amplitude decibels")) {
            scale = "RELATIVE_DB";
        } else if (scale.equals("relative power decibels")) {
            scale = "RELATIVE_DB_POWER";
        } else if (scale.equals("relative linear")) {
            scale = "RELATIVE_LINEAR";
        }

        ArrayList args = new ArrayList();
        args.add(scale);
        _codeStream.appendCodeBlock("fireBlock", args);

        return processCode(_codeStream.toString());
    }

    /** Get the files needed by the code generated for the RandomSource actor.
     *  @return A set of strings that are names of the files
     *  needed by the code generated for the RandomSource actor.
     *  @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = super.getHeaderFiles();
        files.add("<math.h>");
        return files;
    }
}
