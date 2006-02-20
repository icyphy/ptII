/* A helper class for ptolemy.actor.lib.MonitorValue

 Copyright (c) 2005 The Regents of the University of California.
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

import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

/** A helper class for ptolemy.actor.lib.MonitorValue.
 *
 *  @author Gang Zhou
 *  @version $Id$
 *  @since Ptolemy II 5.2
 *  @Pt.ProposedRating Red (zgang)
 *  @Pt.AcceptedRating Red (zgang)
 */
public class MonitorValue extends CCodeGeneratorHelper {
    
    /** Constructor method for the MonitorValue helper.
     *  @param actor the associated actor
     */
    public MonitorValue(ptolemy.actor.lib.MonitorValue actor) {
        super(actor);
    }

    /** Generate fire code.
     *  The method reads in <code>fireBlock</code> from MonitorValue.c,
     *  replaces macros with their values and appends the processed code
     *  block to the given code buffer.
     *  @return The generated code.
     *  @exception IllegalActionException If the code stream encounters an
     *   error in processing the specified code blocks or the type is not supported.
     */
    public String generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateFireCode());
        
        ptolemy.actor.lib.MonitorValue actor = (ptolemy.actor.lib.MonitorValue) getComponent();
        Type type = actor.input.getType();
        for (int i = 0; i < actor.input.getWidth(); i++) {
            ArrayList args = new ArrayList();
            args.add(new Integer(i));
            args.add(actor.getName());
            if (type == BaseType.INT || type == BaseType.BOOLEAN) {
                code.append(_generateBlockCode("intBlock", args));
            } else if (type == BaseType.DOUBLE) {
                code.append(_generateBlockCode("doubleBlock", args));
            } else if (type == BaseType.STRING) {
                code.append(_generateBlockCode("stringBlock", args));
            } else {
                throw new IllegalActionException(actor, "The type: " 
                        + type + " is not supported for now.");
            }
        }
        
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
