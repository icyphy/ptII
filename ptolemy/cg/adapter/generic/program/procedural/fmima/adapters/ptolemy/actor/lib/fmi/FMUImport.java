/* An adapter class for ptolemy.actor.lib.fmi.FMUImport

 Copyright (c) 2012-2013 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

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
package ptolemy.cg.adapter.generic.program.procedural.fmima.adapters.ptolemy.actor.lib.fmi;

import ptolemy.actor.TypedIOPort;
import ptolemy.cg.adapter.generic.program.procedural.fmima.adapters.ptolemy.actor.Director;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.procedural.fmima.FMIMACodeGeneratorAdapter;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// FMUImport

/**
 An adapter class for ptolemy.actor.lib.fmi.FMUImport.

 @author Christopher Brooks
 @version $Id: FMUImport.java 67784 2013-10-26 16:53:27Z cxh $
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class FMUImport extends FMIMACodeGeneratorAdapter {
    /**
     *  Construct the FMUImport adapter.
     *  @param actor the associated actor
     */
    public FMUImport(ptolemy.actor.lib.fmi.FMUImport actor) {
        super(actor);
    }

    /** Generate FMIMA code.
     *  @return The generated FMIMA.
     *  @exception IllegalActionException If there is a problem getting the adapter, getting
     *  the director or generating FMIMA for the director.
     */
    public String generateFMIMA() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        //NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getAdapter(getContainer());
        ptolemy.actor.lib.fmi.FMUImport actor = (ptolemy.actor.lib.fmi.FMUImport) getComponent();
        code.append(actor.getName() + " is a FMUImport s: ");
        code.append("<ul>" + _eol);

        for (TypedIOPort input : actor.inputPortList()) {
            code.append("<li> input " + input.getName() + "</li>" + _eol);
        }

        for (TypedIOPort output : actor.outputPortList()) {
            code.append("<li> output " + output.getName() + "</li>" + _eol);
        }

        code.append("</ul>" + _eol);
        return /*processCode(code.toString())*/code.toString();
    }

}
