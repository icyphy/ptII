/*
 @Copyright (c) 1998-2014 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */
package ptolemy.domains.sdf.kernel.test;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * This actor deterministically splits its input token stream into two
 * streams.
 * @author Steve Neuendorffer
 * @version $Id$
 * @since Ptolemy II 0.4
 * @Pt.ProposedRating Red
 * @Pt.AcceptedRating Red
 */
public class SDFTestSplit extends TypedAtomicActor {
    public SDFTestSplit(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        input_tokenConsumptionRate = new Parameter(input,
                "tokenConsumptionRate", new IntToken(2));
        input.setTypeEquals(BaseType.INT);

        output1 = new TypedIOPort(this, "output1", false, true);
        output1_tokenProductionRate = new Parameter(output1,
                "tokenProductionRate", new IntToken(1));
        output1.setTypeEquals(BaseType.INT);

        output2 = new TypedIOPort(this, "output2", false, true);
        output2_tokenProductionRate = new Parameter(output2,
                "tokenProductionRate", new IntToken(1));

        output2.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    public TypedIOPort input;

    public TypedIOPort output1;

    public TypedIOPort output2;

    public Parameter input_tokenConsumptionRate;

    public Parameter output1_tokenProductionRate;

    public Parameter output2_tokenProductionRate;

    /**
     * Consume two tokens from the input.  Copy the first one to the port
     * output1, and the second to the port output2
     * @exception IllegalActionException if a contained method throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        IntToken message;

        message = (IntToken) input.get(0);
        output1.send(0, message);
        message = (IntToken) input.get(0);
        output2.send(0, message);
    }
}
