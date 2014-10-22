/* A code generation adapter class for domains.sdf.lib.FFT
 @Copyright (c) 2007-2014 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.java.adapters.ptolemy.domains.sdf.lib;

import java.util.Set;

import ptolemy.cg.kernel.generic.program.procedural.ProceduralCodeGenerator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.StringUtilities;

/**
 A code generation adapter class for ptolemy.domains.sdf.lib.FFT.

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class FFT
        extends
        ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.sdf.lib.FFT {

    /**
     * Construct a FFT adapter.
     * @param actor The associated actor.
     */
    public FFT(ptolemy.domains.sdf.lib.FFT actor) {
        super(actor);
    }

    /** Get the classes needed by the code generated for the FFT actor.
     *  @return A set of strings that are names of the classes
     *  needed by the code generated for the FFT actor.
     *  @exception IllegalActionException Not Thrown in this subclass.
     */
    @Override
    public Set getHeaderFiles() throws IllegalActionException {
        ((ProceduralCodeGenerator) getCodeGenerator())
                .addLibraryIfNecessary(StringUtilities
                        .getProperty("ptolemy.ptII.dir"));
        Set files = super.getHeaderFiles();
        files.add("ptolemy.math.Complex;");
        files.add("ptolemy.math.SignalProcessing;");
        return files;
    }
}
