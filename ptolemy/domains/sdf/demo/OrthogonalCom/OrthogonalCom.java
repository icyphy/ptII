/* An binary, orthogonal communication system.

Copyright (c) 1998-2009 The Regents of the University of California.
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

package ptolemy.domains.sdf.demo.OrthogonalCom;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIORelation;
import ptolemy.actor.lib.AddSubtract;
import ptolemy.actor.lib.Const;
import ptolemy.actor.lib.DiscreteRandomSource;
import ptolemy.actor.lib.Gaussian;
import ptolemy.actor.lib.Maximum;
import ptolemy.actor.lib.Multiplexor;
import ptolemy.actor.lib.io.ExpressionWriter;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.domains.sdf.lib.DotProduct;
import ptolemy.domains.sdf.lib.SequenceToArray;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
An binary, orthogonal communication system. Randomly choose a bit,
send the signal associated with the bit, add Gaussian noise to
the signal, and attempt to recover the bit using the maximum
likelihood decision rule. The difference between the send and output
bit is output on the screen. (0 = no error; 1,-1 = error).

This class is used as a demonstration of the codegen facility, which is
why it is written in Java instead of using MoML.

@author Jeff Tsay
@version $Id$
@since Ptolemy II 8.0
@version $Id$
@Pt.ProposedRating Red (ctsay)
@Pt.AcceptedRating Red (ctsay)
*/
public class OrthogonalCom extends TypedCompositeActor {

    /** Construct the orthogonal communication system.
     *  @param w The workspace in which to construct the system.
     *  @exception IllegalActionException If the system cannot be
     *  constructed.
     */
    public OrthogonalCom(Workspace w) throws IllegalActionException {
        super(w);

        try {
            setDirector(new SDFDirector(this, "director"));

            // Bit source
            DiscreteRandomSource bitSource = new DiscreteRandomSource(this,
                    "bitSource");

            // Signals
            Const signal1 = new Const(this, "signal1");
            // signal1.value.setToken(new DoubleMatrixToken(
            //         new double[][] {{ 1, 1, 1, 1, 1, 1, 1, 1 }}));
            signal1.value.setExpression("{1, 1, 1, 1, 1, 1, 1, 1}");

            Const signal2 = new Const(this, "signal2");
            // signal2.value.setToken(new DoubleMatrixToken(
            //         new double[][] {{ 1, 1, 1, 1, -1, -1, -1, -1 }}));
            signal2.value.setExpression("{1, 1, 1, 1, -1, -1 ,-1 ,-1}");

            // Signal selector
            Multiplexor mux = new Multiplexor(this, "mux");

            // Adder
            AddSubtract adder = new AddSubtract(this, "adder");

            // Gaussian noise
            Gaussian noise = new Gaussian(this, "noise");
            noise.standardDeviation.setToken(new DoubleToken(2.0));

            // Convert noise samples into matrix.
            // SequenceToDoubleMatrix noisePacker =
            //     new SequenceToDoubleMatrix(this, "noisePacker");
            SequenceToArray noisePacker = new SequenceToArray(this,
                    "noisePacker");

            // Pack 8 samples into each matrix.
            // noisePacker.columns.setToken(new IntToken(8));
            noisePacker.arrayLength.setToken(new IntToken(8));

            // Correlators
            DotProduct correlator1 = new DotProduct(this, "correlator1");
            DotProduct correlator2 = new DotProduct(this, "correlator2");

            // Decision
            Maximum decision = new Maximum(this, "decision");

            // Displays
            ExpressionWriter outputBitDisplay = new ExpressionWriter(this,
                    "outputBitDisplay");

            AddSubtract diff = new AddSubtract(this, "diff");

            // Connect everything up.
            TypedIORelation r0 = (TypedIORelation) newRelation("r0");
            bitSource.output.link(r0);
            mux.select.link(r0);
            diff.plus.link(r0);

            TypedIORelation r1 = (TypedIORelation) newRelation("r1");
            signal1.output.link(r1);
            mux.input.link(r1);
            correlator1.input1.link(r1);

            TypedIORelation r2 = (TypedIORelation) newRelation("r2");
            signal2.output.link(r2);
            mux.input.link(r2);
            correlator2.input1.link(r2);

            TypedIORelation r3 = (TypedIORelation) newRelation("r3");
            adder.output.link(r3);
            correlator1.input2.link(r3);
            correlator2.input2.link(r3);

            connect(noise.output, noisePacker.input);

            connect(mux.output, adder.plus);
            connect(noisePacker.output, adder.plus);

            connect(correlator1.output, decision.input);
            connect(correlator2.output, decision.input);

            connect(decision.maximumValue, diff.minus);

            connect(diff.output, outputBitDisplay.input);

            // A hack to get code generation to work.
            outputBitDisplay.input.setTypeEquals(BaseType.INT);

            // Uncomment the next line dump out xml.
            // System.out.println(exportMoML());

        } catch (NameDuplicationException nde) {
            throw new RuntimeException(nde.toString());
        }
    }
}
