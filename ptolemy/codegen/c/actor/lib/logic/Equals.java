/* A code generation helper class for actor.lib.logic.Equals
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
 PROVIDED HEREUNDER IS ON AN \"AS IS\" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, 
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.codegen.c.actor.lib.logic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 A code generation helper class for ptolemy.actor.lib.logic.Equals. 

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (mankit) Works with Doubles and Strings, Needs to work with Arrays
 @Pt.AcceptedRating Red (mankit)
 */
public class Equals extends CCodeGeneratorHelper {

    /**
     * Constructor method for the Equals helper.
     * @param actor The associated actor.
     */
    public Equals(ptolemy.actor.lib.logic.Equals actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * Read the <code>fireBlock</code> from Equals.c,
     * replace macros with their values and append the processed code              
     * block to the given code buffer.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateFireCode() throws IllegalActionException {
        super.generateFireCode();
        ptolemy.actor.lib.logic.Equals actor = (ptolemy.actor.lib.logic.Equals) getComponent();

        String type = "";
        type = codeGenType(actor.input.getType());
        if (!_isPrimitiveType(type)) {
            type = "Token";
        } 
        
        _codeStream.appendCodeBlock("fireBlockOpen");
        ArrayList args = new ArrayList();
        args.add(new Integer(0));
        args.add(new Integer(1));
        for (int i = 0; i < actor.input.getWidth() - 1; i++) {
            args.set(0, new Integer(i));
            args.set(1, new Integer(i+1));
            _codeStream.appendCodeBlock(type + "EqualsBlock", args);
        }
        return processCode(_codeStream.toString());
    }

    /**
     * Get the files needed by the code generated for the
     * Equals actor.
     * @return A set of Strings that are names of the header files
     *  needed by the code generated for the Equals actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = new HashSet();
        files.addAll(super.getHeaderFiles());
        files.add("<string.h>");
        return files;
    }
}
