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
package ptolemy.cg.adapter.generic.program.procedural.c.renesas.adapters.ptolemy.actor.lib;

import java.util.LinkedList;
import java.util.List;

import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
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
public class TimeDelay
        extends
        ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.actor.lib.TimeDelay {
    /**
     *  Construct a TimeGap adapter.
     *  @param actor The given ptolemy.actor.lib.TimeGap actor.
     */
    public TimeDelay(ptolemy.actor.lib.TimeDelay actor) {
        super(actor);
    }

    @Override
    public String generateFireCode() throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.clear();
        List<String> args = new LinkedList();
        Parameter delay = ((ptolemy.actor.lib.TimeDelay) getComponent()).delay;
        double value = ((DoubleToken) delay.getToken()).doubleValue();
        args.add(Double.toString(value));
        codeStream.appendCodeBlock("fireBlock", args);
        return processCode(codeStream.toString());
    }

    /** Return the value of the delay parameter.
     *  @return the value of the delay parameter in the target
     *  language.
     *  @exception IllegalActionException If thrown while reading the
     *  <i>delay</i> parameter.
     */
    @Override
    public String getAddTimeString() throws IllegalActionException {
        return getParameterValue("delay", (NamedObj) _component);
    }
}
