/* Ramp using arrays and printer demo for use with Codegen

Copyright (c) 2001 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/


package ptolemy.domains.sdf.codegen.test;

import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.domains.sdf.lib.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

/** A very simple demo that connects an integer array Ramp source to
 *  a Printer.
 */
public class RampArraySystem extends TypedCompositeActor {

    public RampArraySystem(Workspace w) throws IllegalActionException {
        super(w);

        try {
            setDirector(new SDFDirector(this, "director"));
            Ramp ramp = new Ramp(this, "ramp");
            ramp.init.setExpression("{0.0,0.1}");
            ramp.step.setExpression("{1,2}");
            FileWriter fileWriter = new FileWriter(this, "fileWriter");
            connect(ramp.output, fileWriter.input);

            // A hack to get code generation to work
            //fileWriter.input.setTypeEquals(BaseType.INT);
        } catch (NameDuplicationException e) {
            throw new RuntimeException(e.toString());
        }
    }
}
