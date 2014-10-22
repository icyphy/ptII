/* DoubleFork is a simple DDE actor with one input and two outputs.

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
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.domains.dde.kernel.DDEReceiver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// DoubleFork

/**
 DoubleFork is a simple DDE actor with one input and two outputs. It
 has two multiport outputs - "output1" and "output2." The fire method of
 DoubleFork passes real tokens through the first output (output1).
 Real tokens are never produced on output2.

 @author John S. Davis II
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Red (davisj)
 @Pt.AcceptedRating Red (cxh)

 */
public class DoubleFork extends TypedAtomicActor {
    /** Construct a DoubleFork actor with the specified container
     *  and name.
     * @param container The TypedCompositeActor that contains this actor.
     * @param name The name of this actor.
     * @exception NameDuplicationException If the name of this actor
     *  duplicates that of a actor already contained by the container
     *  of this actor.
     * @exception IllegalActionException If there are errors in
     *  instantiating and specifying the type of this actor's ports.
     */
    public DoubleFork(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        output1 = new TypedIOPort(this, "output1", false, true);
        output1.setMultiport(true);
        output1.setTypeEquals(BaseType.GENERAL);
        output2 = new TypedIOPort(this, "output2", false, true);
        output2.setMultiport(true);
        output2.setTypeEquals(BaseType.GENERAL);
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(BaseType.GENERAL);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The first output port.
     */
    public TypedIOPort output1;

    /** The second output port.
     */
    public TypedIOPort output2;

    /** The input port.
     */
    public TypedIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute this actor by consuming a real input token and producing
     *  an equivalent real token on output1.
     * @exception IllegalActionException If there are errors in obtaining
     *  the receivers of this actor.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Token token = null;
        Receiver[][] inputReceivers = input.getReceivers();

        if (inputReceivers.length == 0) {
            _continueIterations = false;
        }

        for (Receiver[] inputReceiver2 : inputReceivers) {
            for (int j = 0; j < inputReceiver2.length; j++) {
                DDEReceiver inputReceiver = (DDEReceiver) inputReceiver2[j];

                if (inputReceiver.hasToken()) {
                    token = inputReceiver.get();
                    output1.broadcast(token);
                }
            }
        }
    }

    /** Return true if this actor will allow subsequent iterations to
     *  occur; return false otherwise.
     * @return False if super.postfire() returns false, true if
     * continued execution is enabled; false otherwise.
     * @exception IllegalActionException If thrown by the parent class.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        // This is similar to DDESink, Wire.
        if (!super.postfire()) {
            return false;
        }
        return _continueIterations;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private boolean _continueIterations = true;
}
