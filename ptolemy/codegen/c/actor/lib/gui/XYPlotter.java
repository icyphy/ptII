/*
 @Copyright (c) 2005 The Regents of the University of California.
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
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY


 */

package ptolemy.codegen.c.actor.lib.gui;

import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.c.actor.lib.CodeStream;
import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A helper class for ptolemy.actor.lib.gui.XYPlotter.
 * 
 * @author Jackie
 * @version $Id$
 * @since Ptolemy II 5.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class XYPlotter extends CCodeGeneratorHelper {
    /**
     * Constructor method for the XYPlotter helper.
     * @param actor the associated actor.
     */
    public XYPlotter(ptolemy.actor.lib.gui.XYPlotter actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method reads in <code>writeFile</code> from XYPlotter.c 
     * replaces macros with their values and appends to the given code buffer.
     * @param code the given buffer to append the code to.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public void generateFireCode(StringBuffer code)
            throws IllegalActionException {
        // FIXME: how do we add legend to the file??
        CodeStream _codeStream = new CodeStream(this);
        _codeStream.appendCodeBlock("writeFile");
        code.append(processCode(_codeStream.toString()));
    }

    /**
     * Generate initialize code.
     * This method reads the <code>initBlock</code> from XYPlotter.c,
     * replaces macros with their values and returns the processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     * @return The processed code block.
     */
    public String generateInitializeCode() throws IllegalActionException {
        super.generateInitializeCode();
        CodeStream _codeStream = new CodeStream(this);
        _codeStream.appendCodeBlock("initBlock");
        return processCode(_codeStream.toString());
    }

    /** 
     * Generate preinitialize code.
     * This method reads the <code>preinitBlock</code> from XYPlotter.c,
     * replaces macros with their values and returns the processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     * @return The processed code block.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        super.generatePreinitializeCode();
        return _generateBlockCode("preinitBlock");
    }

    /** 
     * Generate wrap up code.
     * This method reads the <code>closeFile</code> and <code>graphPlot</code>
     * from XYPlotter.c, replaces macros with their values and appends to the
     * given code buffer.
     * @param code the given buffer to append the code to.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public void generateWrapupCode(StringBuffer code)
            throws IllegalActionException {

        ptolemy.actor.lib.gui.XYPlotter actor = 
            (ptolemy.actor.lib.gui.XYPlotter) getComponent();

        _codeStream.clear();
        _codeStream.appendCodeBlock("closeFile");

        if (actor.fillOnWrapup.getExpression().equals("true")) {
            _codeStream.appendCodeBlock("graphPlot");
        }
        code.append(processCode(_codeStream.toString()));
    }

    /** 
     * Get the files needed by the code generated for the
     * XYPlotter actor.
     * @return A set of strings that are names of the files
     *  needed by the code generated for the XYPlotter actor.
     */
    public Set getHeaderFiles() {
        Set files = new HashSet();
        files.add("\"stdio.h\"");
        return files;
    }
}
