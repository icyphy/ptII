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

///////////////////////////////////////////////////////////////////
//// SDFTestJoin

/**
 * A deterministic merge of two token streams.
 * @author Stephen Neuendorffer
 * @version $Id$
 * @since Ptolemy II 0.4
 * @Pt.ProposedRating Red
 * @Pt.AcceptedRating Red
 */
public class SDFTestJoin extends TypedAtomicActor {
    public SDFTestJoin(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input1 = new TypedIOPort(this, "input1", true, false);
        input1_tokenConsumptionRate = new Parameter(input1,
                "tokenConsumptionRate", new IntToken(1));
        input1.setTypeEquals(BaseType.GENERAL);

        input2 = new TypedIOPort(this, "input2", true, false);
        input2_tokenConsumptionRate = new Parameter(input2,
                "tokenConsumptionRate", new IntToken(1));
        input2.setTypeEquals(BaseType.GENERAL);

        output = new TypedIOPort(this, "output", false, true);
        output_tokenProductionRate = new Parameter(output,
                "tokenProductionRate", new IntToken(2));

        output.setTypeEquals(BaseType.GENERAL);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    public TypedIOPort input1;

    public TypedIOPort input2;

    public TypedIOPort output;

    public Parameter input1_tokenConsumptionRate;

    public Parameter input2_tokenConsumptionRate;

    public Parameter output_tokenProductionRate;

    /** Fire the actor.
     * Copy one token from input1 to the output and then copy one token
     * from input2 to the output.
     * @exception IllegalActionException If a contained method throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        IntToken message;

        message = (IntToken) input1.get(0);
        output.send(0, message);
        message = (IntToken) input2.get(0);
        output.send(0, message);
    }
}
