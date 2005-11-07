/* A code generation helper class for actor.lib.gui.Display

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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import ptolemy.actor.TypedIOPort;
import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A code generation helper class for ptolemy.actor.lib.gui.Display.
 *
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 5.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class Display extends CCodeGeneratorHelper {
    /**
     * Constructor method for the Display helper.
     * @param actor The associated actor.
     */
    public Display(ptolemy.actor.lib.gui.Display actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method reads in <code>printInt</code>, <code>printArray</code>,
     * <code>printString</code>, or <code>printDouble</code> from Display.c,
     * replaces macros with their values and appends the processed code
     * block to the given code buffer.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateFireCode());

        ptolemy.actor.lib.gui.Display actor = 
            (ptolemy.actor.lib.gui.Display) getComponent();
        _codeStream.clear();

        String type = "";
        if (actor.input.getType() == BaseType.INT) {
            type = "Int";
        } else if (actor.input.getType() == BaseType.DOUBLE) {
            type = "Double";
        } else if (actor.input.getType() == BaseType.STRING) {
            type = "String";
        } else {
            type = "Token";
        }
        
        ArrayList args = new ArrayList();
        args.add(new Integer(0));
        for (int i = 0; i < actor.input.getWidth(); i++) {
            TypedIOPort port = 
                (TypedIOPort) actor.input.sourcePortList().get(i);

            args.set(0, Integer.toString(i));
            _codeStream.appendCodeBlock("print" + type, args);
        }
        code.append(processCode(_codeStream.toString()));
        
        return code.toString();
    }

    /**
     * Get the files needed by the code generated for the
     * Display actor.
     * @return A set of strings that are names of the header files
     *  needed by the code generated for the Display actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        super.getHeaderFiles();

        Set files = new HashSet();
        files.add("\"stdio.h\"");
        return files;
    }
}
