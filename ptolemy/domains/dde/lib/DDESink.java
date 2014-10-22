/* DDESink is simple DDE actor that consumes real tokens.

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
package ptolemy.domains.dde.lib;

import ptolemy.actor.Receiver;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.dde.kernel.DDEReceiver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// DDESink

/**
 DDESink is simple DDE actor that consumes real tokens. This actor
 has a parameter named 'numTokens' that specifies the number of
 real tokens that this actor will consume. If numTokens is set to
 a negative value, then this actor will continue to consume tokens
 as long as they are available. If numTokens is set to a non-negative
 value 'k', then this actor will consume k tokens.

 @author John S. Davis II
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Red (davisj)
 @Pt.AcceptedRating Red (cxh)

 */
public class DDESink extends TypedAtomicActor {
    /** Construct a DDESink with the specified container and name.
     * @param container The container of this actor.
     * @param name The name of this actor.
     * @exception NameDuplicationException If the name of this actor
     *  duplicates that of a actor already contained by the container
     *  of this actor.
     * @exception IllegalActionException If there are errors in
     *  instantiating and specifying the type of this actor's ports.
     */
    public DDESink(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(BaseType.GENERAL);

        numTokens = new Parameter(this, "numTokens", new IntToken(-1));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port.
     */
    public TypedIOPort input;

    /** Indicate the integer valued number of real tokens that this
     *  actor should consume. If this value is negative, then there
     *  is no limit on the number of tokens that this actor will
     *  consume. The default value of this parameter is -1.
     */
    public Parameter numTokens;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute this actor by consuming a real token. If the numTokens
     *  parameter has been set to a non-negative value 'k', then stop
     *  iterating after 'k' real tokens have been consumed. If the
     *  value of numTokens is negative, then continue consuming real
     *  tokens as long as they are available.
     * @exception IllegalActionException If there is an exception while
     *  accessing the receivers of this actor.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        int value = ((IntToken) numTokens.getToken()).intValue();

        if (value >= 0) {
            _count++;

            if (_count > value) {
                _continueIteration = false;
                return;
            }
        }

        Receiver[][] receivers = input.getReceivers();

        for (Receiver[] receiver2 : receivers) {
            for (int j = 0; j < receiver2.length; j++) {
                DDEReceiver receiver = (DDEReceiver) receiver2[j];

                if (!_continueIteration) {
                    return;
                } else if (receiver.hasToken()) {
                    receiver.get();
                }
            }
        }
    }

    /** Return true if this actor is enabled to proceed with additional
     *  iterations. Return false otherwise.
     * @return False if super.postfire() returns false, true if
     * continued execution is enabled; false otherwise.
     * @exception IllegalActionException If thrown by the parent class.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        // This is similar to DoubleFork, Wire.
        if (!super.postfire()) {
            return false;
        }
        return _continueIteration;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private int _count = 0;

    private boolean _continueIteration = true;
}
