/* A helper class for ptolemy.actor.lib.gui.SliderSource.

 Copyright (c) 2006-2009 The Regents of the University of California.
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
import java.util.Set;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// SliderPlotter

/**
 A helper class for ptolemy.actor.lib.gui.SliderSource.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (zgang)
 @Pt.AcceptedRating Red (zgang)
 */
public class SliderSource extends CCodeGeneratorHelper {

    /** Constructor method for the SliderSource helper.
     *  @param actor the associated actor.
     */
    public SliderSource(ptolemy.actor.lib.gui.SliderSource actor) {
        super(actor);
    }

    /** Generate initialize code.
     *  @return The generated code.
     *  @exception IllegalActionException If the code stream encounters
     *   errors in processing the specified code blocks.
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        ArrayList args = new ArrayList();
        String ptIIDir = StringUtilities.getProperty("ptolemy.ptII.dir")
                .replace('\\', '/');
        args.add(ptIIDir);
        code.append(_generateBlockCode("createJVMBlock", args));

        code.append(super.generateInitializeCode());

        return code.toString();
    }

    /** Get the header files needed by the code generated for the
     *  SliderSource actor.
     *  @return A set of strings that are names of the header files
     *   needed by the code generated for the SliderSource actor.
     *  @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = super.getHeaderFiles();
        files.addAll(getJVMHeaderFiles());
        return files;
    }
}
