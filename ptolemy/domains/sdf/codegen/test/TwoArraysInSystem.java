/* Test out TwoArraysIn

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

/** A very simple demo that connects two Consts to a TwoArraysIn actor to
 *  a Printer.
 */
public class TwoArraysInSystem extends TypedCompositeActor {

    public TwoArraysInSystem(Workspace w) throws IllegalActionException {
        super(w);

        try {
            setDirector(new SDFDirector(this, "director"));
            Const const1 = new Const(this, "const1");
            const1.value.setExpression("{1.0,2.0}");

            Const const2 = new Const(this, "const2");
            const2.value.setExpression("{2.0,3.0}");

            TwoArraysIn twoArraysIn = new TwoArraysIn(this, "twoArraysIn");
            connect(const1.output, twoArraysIn.input1);
            connect(const2.output, twoArraysIn.input2);

            FileWriter fileWriter = new FileWriter(this, "fileWriter");
            connect(twoArraysIn.output, fileWriter.input);

        } catch (NameDuplicationException e) {
            throw new RuntimeException(e.toString());
        }
    }
}
