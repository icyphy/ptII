/* An actor that throws an exception when it receives a true token.

 Copyright (c) 1997-2014 The Regents of the University of California.
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

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// ThrowException

/**
 An actor that throws an IllegalActionException when it receives a true token
 on any input channel.  The message reported in the exception is
 given by the <i>message</i> parameter.
 By default, inputs are read and checked in the fire() method, but if
 <i>throwInPostfire</i> is set to true, then they will be checked in postfire() only.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (neuendor)
 @see ThrowModelError
 */
public class ThrowException extends Sink {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public ThrowException(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input.setTypeEquals(BaseType.BOOLEAN);

        message = new StringAttribute(this, "message");
        message.setExpression("Model triggered an exception.");

        throwInPostfire = new Parameter(this, "throwInPostfire");
        throwInPostfire.setTypeEquals(BaseType.BOOLEAN);
        throwInPostfire.setExpression("false");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The message reported in the exception, which is a string that
     *  defaults to "Model triggered an exception".
     */
    public StringAttribute message;

    /** True to throw the model error in the postfire method.
     *  False to throw in fire. This is a boolean that defaults
     *  to false.
     */
    public Parameter throwInPostfire;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read one token from each input channel that has a token,
     *  and if any token is true, invoke the model error handler.
     *  @exception IllegalActionException If thrown by the parent class.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (!((BooleanToken) throwInPostfire.getToken()).booleanValue()) {
            boolean result = false;

            // NOTE: We need to consume data on all channels that have data.
            // If we don't then DE will go into an infinite loop.
            for (int i = 0; i < input.getWidth(); i++) {
                if (input.hasToken(i)) {
                    if (((BooleanToken) input.get(i)).booleanValue()) {
                        result = true;
                    }
                }
            }

            if (result) {
                throw new IllegalActionException(this, message.getExpression());
            }
        }
    }

    /** Read one token from each input channel that has a token,
     *  and if any token is true, throw an exception.
     *  @exception IllegalActionException If FIXME
     *  @return Whatever the base class returns (probably true).
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (((BooleanToken) throwInPostfire.getToken()).booleanValue()) {
            boolean result = false;

            // NOTE: We need to consume data on all channels that have data.
            // If we don't then DE will go into an infinite loop.
            for (int i = 0; i < input.getWidth(); i++) {
                if (input.hasToken(i)) {
                    if (((BooleanToken) input.get(i)).booleanValue()) {
                        result = true;
                    }
                }
            }

            if (result) {
                throw new IllegalActionException(this, message.getExpression());
            }
        }
        return super.postfire();
    }
}
