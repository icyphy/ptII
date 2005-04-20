/* A code generation helper class for ptolemy.domains.sdf.lib.SampleDelay

Copyright (c) 1997-2005 The Regents of the University of California.
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
package ptolemy.codegen.c.domains.sdf.lib;

import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.codegen.c.actor.lib.CodeStream;
import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.data.ArrayToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// SampleDelay
/**
A code generation helper class for ptolemy.domains.sdf.lib.SampleDelay

@author Ye Zhou
@version $Id$
@since Ptolemy II 5.0
@Pt.ProposedRating Red (eal)
@Pt.AcceptedRating Red (eal)
*/

public class SampleDelay extends CCodeGeneratorHelper {

    /** Construct a helper with the given 
     *  ptolemy.domains.sdf.lib.SampleDelay actor.
     *  @param actor The given ptolemy.domains.sdf.lib.SampleDelay actor.
     */
    public SampleDelay(ptolemy.domains.sdf.lib.SampleDelay actor) {
        super(actor);
    }

    ////////////////////////////////////////////////////////////////////
    ////                     public methods                         ////

    /** Generate the fire code of the SampleDelay actor.
     *  @param stream The string buffer to which the fire code of the
     *   SampleDelay actor is appended to.
     */
    public void generateFireCode(StringBuffer stream)
            throws IllegalActionException {
        CodeStream tmpStream = new CodeStream(this);
        tmpStream.appendCodeBlock("codeBlock1");
        stream.append(processCode(tmpStream.toString()));
        //stream.append(processCode("$ref(output) = $ref(input);\n"));
    }

    /** Generate the initialize code for the SampleDelay actor by
     *  declaring the initial values of the sink channels of the
     *  output port of the SampleDelay actor.
     *  @return The generated initialize code for the SampleDelay actor.
     *  @exception IllegalActionException If the base class throws it,
     *   or if the "bufferSize" attribute of the relation connected to
     *   the SampleDelay output port is not defined, or if the initial
     *   outputs of the SampleDelay actor is not defined.
     */
    public String generateInitializeCode() throws IllegalActionException {
        super.generateInitializeCode();
        StringBuffer code = new StringBuffer();
        ptolemy.domains.sdf.lib.SampleDelay actor =
            (ptolemy.domains.sdf.lib.SampleDelay) getComponent();
        Token[] initialOutputs = 
                ((ArrayToken)actor.initialOutputs.getToken()).arrayValue();
        List sinkChannels = getSinkChannels(actor.output, 0);
        for (int i = 0; i < initialOutputs.length; i ++) {
            for (int j = 0; j < sinkChannels.size(); j ++) {
                Channel channel = (Channel) sinkChannels.get(j);
                IOPort port = (IOPort) channel.port;
                code.append(port.getFullName().replace('.', '_'));
                if (port.isMultiport()) {
                    code.append("[" + channel.channelNumber + "]");
                }
                int bufferSize = getBufferSize(port);
                if (bufferSize > 1) {
                    code.append("[" + i + "]");
                }
                code.append(" = ");
            }
            code.append(initialOutputs[i].toString() + ";\n");
        }
        // FIXME: Do we need /Should we update the offset of input of
        // the SampleDelay?
        setOffset(actor.input, 0, new Integer(initialOutputs.length));
        setOffset(actor.output, 0, new Integer(initialOutputs.length));
        return code.toString();
    }
}
