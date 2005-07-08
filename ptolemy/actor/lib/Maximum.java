/* An actor that outputs the maximum of all the inputs.

 Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.actor.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// Maximum

/**
 Read at most one token from each input channel and broadcast the one with the
 greatest value to the <i>maximumValue</i> output.
 In addition, broadcast the channel number of the maximum on
 the <i>channelNumber</i> output port.  Either output port may be
 left unconnected if you do not need its results (this is why these
 are multiports).
 This actor works with any scalar token. For ComplexToken, the output is
 the one with the maximum magnitude.
 The input port is a multiport.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Yellow (yuhong)
 */
public class Maximum extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Maximum(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);

        maximumValue = new TypedIOPort(this, "maximumValue", false, true);
        maximumValue.setMultiport(true);
        maximumValue.setTypeAtMost(BaseType.SCALAR);
        maximumValue.setTypeAtLeast(input);

        channelNumber = new TypedIOPort(this, "channelNumber", false, true);
        channelNumber.setMultiport(true);
        channelNumber.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port.  This base class imposes no type constraints except
     *  that the type of the input cannot be greater than the type of the
     *  <i>maximumValue</i> output.
     */
    public TypedIOPort input;

    /** The output port for the maximum value. The type of this
     *  output is constrained to be at most a scalar.
     */
    public TypedIOPort maximumValue;

    /** The output port for the channel number. The type of this
     *  output is an integer.
     */
    public TypedIOPort channelNumber;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Maximum newObject = (Maximum) super.clone(workspace);
        newObject.maximumValue.setTypeAtMost(BaseType.SCALAR);
        newObject.maximumValue.setTypeAtLeast(newObject.input);
        newObject.channelNumber.setTypeEquals(BaseType.INT);
        return newObject;
    }

    /** Read at most one token from each input channel and broadcast the one
     *  with the largest value to the <i>maximumValue</i>output.
     *  In addition, broadcast its channel number to the <i>channelNumber</i>
     *  output.  If there is no input, then produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        ScalarToken result = null;
        int channelNum = -1;

        for (int i = 0; i < input.getWidth(); i++) {
            if (input.hasToken(i)) {
                ScalarToken in = (ScalarToken) input.get(i);

                if (result == null) {
                    result = in;
                    channelNum = i;
                } else {
                    if (result.isLessThan(in).booleanValue() == true) {
                        result = in;
                        channelNum = i;
                    }
                }
            }
        }

        if (result != null) {
            maximumValue.broadcast(result);
            channelNumber.broadcast(new IntToken(channelNum));
        }
    }
}
