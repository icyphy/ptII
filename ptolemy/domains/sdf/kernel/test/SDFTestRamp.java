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
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/** Create an increasing sequence of integer tokens,
 starting with value zero, and incrementing by one.
 This actor is aware of the rate that is set on its port and
 will create the proper number of tokens with every firing.

 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (cxh)
 @author Steve Neuendorffer
 */
public class SDFTestRamp extends TypedAtomicActor {
    public SDFTestRamp(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output = new TypedIOPort(this, "output", false, true);
        output_tokenProductionRate = new Parameter(output,
                "tokenProductionRate", new IntToken(1));
        output.setTypeEquals(BaseType.INT);
        _value = 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    public TypedIOPort output;

    public Parameter output_tokenProductionRate;

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.  The new
     *  actor will have the same parameter values as the old.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        SDFTestRamp newObject = (SDFTestRamp) super.clone(workspace);
        newObject.output = (TypedIOPort) newObject.getPort("output");
        return newObject;
    }

    /**
     * Produce several integer tokens with values with incremental values.
     * The number of tokens produced during each firing is determined by
     * the rates on the ports, and the sequence of values continues across
     * firings.
     * @exception IllegalActionException If a contained method throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        int i;

        int tokens = ((IntToken) output_tokenProductionRate.getToken())
                .intValue();

        for (i = 0; i < tokens; i++) {
            Token message = new IntToken(_value);
            _value = _value + 1;
            output.send(0, message);
        }
    }

    /**
     * Initialize the sequence so the first token created has value zero.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _value = 0;
    }

    private int _value;
}
