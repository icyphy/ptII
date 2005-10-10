/* A code generation helper class for actor.lib.ElementsToArray

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
package ptolemy.codegen.c.actor.lib;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.c.actor.lib.CodeStream;
import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A code generation helper class for ptolemy.actor.lib.ElementsToArray.
 *
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 5.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class ElementsToArray extends CCodeGeneratorHelper {

    /**
     * Constructor method for the ElementsToArray helper.
     * @param actor The associated actor.
     */
    public ElementsToArray(ptolemy.actor.lib.ElementsToArray actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method reads in <code>fireBlock</code> from ElementsToArray.c,
     * replaces macros with their values and appends the processed code
     * block to the given code buffer.
     * @param code the given buffer to append the code to.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public void  generateFireCode(StringBuffer code)
        throws IllegalActionException {
        ptolemy.actor.lib.ElementsToArray actor =
            (ptolemy.actor.lib.ElementsToArray) getComponent();
        CodeStream _codeStream = new CodeStream(this);
        ArrayList args = new ArrayList();
        args.add(actor.input.getType().toString());
        args.add(Integer.toString(actor.input.getWidth()));
        
        _codeStream.appendCodeBlock("fireBlock", args);
        code.append(processCode(_codeStream.toString()));
    }

    /**
     * Generate preinitialize code.
     * This method reads the <code>preinitBlock</code> from ElementsToArray.c,
     * replaces macros with their values and returns the processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     * @return The processed code string.
     */
    public String generatePreinitializeCode()
        throws IllegalActionException {
        ptolemy.actor.lib.ElementsToArray actor =
            (ptolemy.actor.lib.ElementsToArray) getComponent();
        super.generatePreinitializeCode();
        CodeStream _codeStream = new CodeStream(this);
        _codeStream.appendCodeBlock("preinitBlock");
        return processCode(_codeStream.toString());
    }

    /**
     * Get the files needed by the code generated for the
     * ElementsToArray actor.
     * @return A set of strings that are names of the header files
     *  needed by the code generated for the ElementsToArray actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = new HashSet();
        files.add("\"stdio.h\"");
        return files;
    }
}
