/* An actor that converts input tokens to specified units.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.conversions;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// InUnitsOf

/**
 <p>An actor that converts input tokens to specified units by dividing the
 input by the value of the <i>units</i> parameter.  This actor is designed
 to be used with a <i>unit system</i>, which must be included in the
 model (note that some Ptolemy II applications do not include unit systems).
 </p><p>
 The units are specified by the <i>units</i> parameter, which contains a
 DoubleToken with units. The input tokens and the token in the <i>unit</i>
 parameter must have the same unit category. Otherwise, an exception
 will be thrown in the fire() method. Unit categories include the ones
 defined in the MoML file, such as length, time, mass, and the composite
 ones formed through the base categories, such as length/time (speed),
 and length * length (area). The output token is a DoubleToken without
 units.</p>

 @author Yuhong Xiong, Xiaojun Liu, Edward Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (yuhong)
 @Pt.AcceptedRating Red (cxh)
 */
public class InUnitsOf extends Transformer {
    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructors, construct
     *  the <i>units</i> parameter. Initialize <i>units</i>
     *  to DoubleToken with value 1.0.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public InUnitsOf(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        units = new Parameter(this, "units", new DoubleToken(1.0));

        // set the type constraints.
        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);
        units.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The units to which the input tokens will be converted.
     *  The default value of this parameter is the double 1.0.
     */
    public Parameter units;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Convert the input to the units specified by the <i>units</i>
     *  parameter.  If there is no input, then produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            DoubleToken in = (DoubleToken) input.get(0);
            DoubleToken out = (DoubleToken) in.inUnitsOf((DoubleToken) units
                    .getToken());
            output.send(0, out);
        }
    }

    /** Return false if the input port has no token, otherwise
     *  return what the superclass returns (presumably true).
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (!input.hasToken(0)) {
            return false;
        }

        return super.prefire();
    }
}
