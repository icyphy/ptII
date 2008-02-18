/* A helper class for actor.lib..vhdl.Multiplexor

 @Copyright (c) 2005-2006 The Regents of the University of California.
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
package ptolemy.codegen.vhdl.actor.lib.vhdl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.vhdl.kernel.VHDLCodeGeneratorHelper;
import ptolemy.data.IntToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.math.Precision;

/**
 * A helper class for ptolemy.actor.lib.Uniform.
 *
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Green (mankit)
 * @Pt.AcceptedRating Green (cxh)
 */
public class Multiplexor extends VHDLCodeGeneratorHelper {
    /**
     * Construct a Concat helper.
     * @param actor the associated actor
     */
    public Multiplexor(ptolemy.actor.lib.vhdl.Multiplexor actor) {
        super(actor);
    }

    /**
     * 
     */
    public Set getSharedCode() throws IllegalActionException {
        Set sharedCode = new HashSet();
        _codeStream.clear();

        ptolemy.actor.lib.vhdl.Multiplexor actor = (ptolemy.actor.lib.vhdl.Multiplexor) getComponent();

        int latencyValue = ((IntToken) actor.latency.getToken()).intValue();

        if (latencyValue == 0) {
            _codeStream.appendCodeBlock("sharedBlock_lat0");
        } else {
            _codeStream.appendCodeBlock("sharedBlock");
        }

        sharedCode.add(processCode(_codeStream.toString()));
        return sharedCode;
    }

    /**
     * Generate fire code.
     * The method reads in the <code>fireBlock</code> from FixConst.c,
     * replaces macros with their values and returns the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters
     *  an error in processing the specified code block(s).
     */
    public String generateFireCode() throws IllegalActionException {
        super.generateFireCode();

        ptolemy.actor.lib.vhdl.Multiplexor actor = (ptolemy.actor.lib.vhdl.Multiplexor) getComponent();

        int latencyValue = ((IntToken) actor.latency.getToken()).intValue();

        ArrayList args = new ArrayList();

        String componentName = (latencyValue == 0) ? "pt_mux2" : "pt_mux2_lat0";

        args.add(componentName);

        Precision outputPrecision = new Precision(
                _getPortPrecision(actor.output));

        int width = outputPrecision.getNumberOfBits();

        args.add(Integer.valueOf(width));

        if (((IntToken) actor.latency.getToken()).intValue() > 0) {

            args.add(actor.latency.getExpression());

            _codeStream.appendCodeBlock("fireBlock", args);
        } else {

            _codeStream.appendCodeBlock("fireBlock_lat0", args);
        }

        return processCode(_codeStream.toString());
    }

    /** Get the files needed by the code generated for the Concat actor.
     *  @return A set of strings that are names of the library and package.
     *  @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = new HashSet();
        files.add("ieee.std_logic_1164.all");
        files.add("ieee.numeric_std.all");
        files.add("ieee_proposed.math_utility_pkg.all");
        files.add("ieee_proposed.fixed_pkg.all");
        files.add("work.pt_utility.all");
        return files;
    }
}
