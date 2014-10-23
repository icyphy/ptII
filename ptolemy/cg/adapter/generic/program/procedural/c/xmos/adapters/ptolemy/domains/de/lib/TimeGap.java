/* An adapter class for ptolemy.domains.de.lib.TimeGap

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.c.xmos.adapters.ptolemy.domains.de.lib;

import java.util.ArrayList;
import java.util.List;

import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// TimeGap

/**
 A adapter class for ptolemy.domains.de.lib.TimeGap.

 @author Jeff C. Jensen
@version $Id$
@since Ptolemy II 10.0
 */
public class TimeGap
extends
ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.de.lib.TimeGap {
    /**
     *  Construct a TimeGap adapter.
     *  @param actor The given ptolemy.actor.lib.TimeGap actor.
     */
    public TimeGap(ptolemy.domains.de.lib.TimeGap actor) {
        super(actor);
    }

    @Override
    public String generateFireCode() throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.clear();
        List<String> args = new ArrayList<String>();
        args.add(CodeGeneratorAdapter.generateName((NamedObj) _component));
        codeStream.appendCodeBlock("fireBlock", args);
        return processCode(codeStream.toString());
    }

    /** Return the name of the port that is the time source.
     *  @return The string "input".
     */
    @Override
    public String getTimeSourcePortName() {
        return "input";
    }
}
