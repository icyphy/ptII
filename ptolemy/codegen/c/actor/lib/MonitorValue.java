/* A helper class for ptolemy.actor.lib.MonitorValue

 Copyright (c) 2005-2006 The Regents of the University of California.
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

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/** A helper class for ptolemy.actor.lib.MonitorValue.
 *
 *  @author Gang Zhou
 *  @version $Id$
 *  @since Ptolemy II 5.2
 *  @Pt.ProposedRating Green (mankit)
 *  @Pt.AcceptedRating Green (cxh)
 */
public class MonitorValue extends CCodeGeneratorHelper {

    /** Construct a MonitorValue helper.
     *  @param actor the associated actor
     */
    public MonitorValue(ptolemy.actor.lib.MonitorValue actor) {
        super(actor);
    }

    /** Generate fire code.
     *  The method reads in <code>fireBlock</code> from MonitorValue.c,
     *  replaces macros with their values and returns the processed code
     *  block.
     *  @return The generated code.
     *  @exception IllegalActionException If the code stream
     *  encounters an error in processing the specified code blocks or
     *  the type is not supported.
     */
    public String generateFireCode() throws IllegalActionException {
        // Note: this actor have the exact same functionality as Display.
        // We want to mirror the Ptolemy actor.lib code structure.
        // The .c files need to remain separate anyway.
        StringBuffer code = new StringBuffer();
        code.append(super.generateFireCode());

        ptolemy.actor.lib.MonitorValue actor = (ptolemy.actor.lib.MonitorValue) getComponent();

        String type = codeGenType(actor.input.getType());
        if (!isPrimitiveType(type)) {
            type = "Token";
        }

        ArrayList args = new ArrayList();
        args.add(actor.getName());
        args.add(new Integer(0));
        for (int i = 0; i < actor.input.getWidth(); i++) {
            args.set(1, Integer.toString(i));
            _codeStream.appendCodeBlock(type + "PrintBlock", args);
        }
        code.append(processCode(_codeStream.toString()));

        return code.toString();
    }

    /** Get the files needed by the code generated for the actor.
     *  @return A set of strings that are names of the header files
     *   needed by the code generated for the actor.
     *  @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = new HashSet();
        files.addAll(super.getHeaderFiles());
        files.add("\"stdio.h\"");
        return files;
    }
}
