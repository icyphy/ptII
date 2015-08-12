/* An actor that outputs the minimum of all the inputs.

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

///////////////////////////////////////////////////////////////////
//// Minimum

/**
 Read at most one token from each input channel and broadcast the one with the
 least value to the <i>minimumValue</i> output.
 In addition, broadcast the channel number of the minimum on
 the <i>channelNumber</i> output port.  Either output port may be
 left unconnected if you do not need its results.
 This actor works with any scalar token. For ComplexToken, the output
 is the one with the minimum magnitude.
 The input port is a multiport.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Yellow (yuhong)
 */
public class Minimum extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Minimum(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);

        minimumValue = new TypedIOPort(this, "minimumValue", false, true);

        /* Note that the output ports need not be multiports since all output
         * channels carry the same data. Using multiports this way is not
         * recommended, so please do not emulate this design in your own actors.
         * Changing this actor to the recommended design would break existing
         * models, so we leave it as is.
         */
        minimumValue.setMultiport(true);
        input.setTypeAtMost(BaseType.SCALAR);
        minimumValue.setTypeAtLeast(input);
        minimumValue.setDefaultWidth(1);

        channelNumber = new TypedIOPort(this, "channelNumber", false, true);

        /* Note that the output ports need not be multiports since all output
         * channels carry the same data. Using multiports this way is not
         * recommended, so please do not emulate this design in your own actors.
         * Changing this actor to the recommended design would break existing
         * models, so we leave it as is.
         */
        channelNumber.setMultiport(true);
        channelNumber.setTypeEquals(BaseType.INT);
        channelNumber.setDefaultWidth(1);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port.  This is required to be a scalar type.
     */
    public TypedIOPort input;

    /** The output port for the minimum value. The type of this
     *  output is constrained to be at least the type of the input.
     */
    public TypedIOPort minimumValue;

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
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Minimum newObject = (Minimum) super.clone(workspace);
        newObject.input.setTypeAtMost(BaseType.SCALAR);
        newObject.minimumValue.setTypeAtLeast(newObject.input);
        newObject.channelNumber.setTypeEquals(BaseType.INT);
        return newObject;
    }

    /** Read at most one token from each input channel and send the one
     *  with the least value to the output.  If there is no input, then
     *  produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        ScalarToken result = null;
        ScalarToken resultMagnitude = null;
        ScalarToken inMagnitude = null;
        int channelNum = -1;

        for (int i = 0; i < input.getWidth(); i++) {
            if (input.hasToken(i)) {
                ScalarToken in = (ScalarToken) input.get(i);

                if (in.getType().equals(BaseType.COMPLEX)) {
                    // If we have a complex, we use the absolute value
                    // for comparison, but save output the initial input
                    // for output at the end.
                    inMagnitude = in.absolute();
                } else {
                    inMagnitude = in;
                }

                if (result == null) {
                    result = in;
                    resultMagnitude = inMagnitude;
                    channelNum = i;
                } else {
                    if (inMagnitude.isLessThan(resultMagnitude).booleanValue() == true) {
                        result = in;
                        resultMagnitude = inMagnitude;
                        channelNum = i;
                    }
                }
            }
        }

        if (result != null) {
            minimumValue.broadcast(result);
            channelNumber.broadcast(new IntToken(channelNum));
        }
    }
}
