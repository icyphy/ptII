/*
Below is the copyright agreement for the Ptolemy II system.
Version: $Id$

Copyright (c) 2007-2014 The Regents of the University of California.
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
 */
package doc.tutorial.domains;

import ptolemy.actor.AbstractReceiver;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;
import ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** Director implementing "amorphous heterogeneity," where each
 *  connection between actors can use a different communication
 *  protocol. This is a demonstration class that shows how
 *  the Receiver class controls the communication between actors.
 *  By default, this director uses an SDFReceiver, which implements
 *  an unbounded FIFO queue. If any input port, however, has a
 *  StringParameter named "receiverClass", then the value of
 *  that parameter gives the class name of a receiver to use
 *  instead of the SDFReceiver.
 *  @author Edward A. Lee
 */
public class AmorphousDirector extends Director {

    /** Constructor. A director is an Attribute.
     *  @param container The container for the director.
     *  @param name The name of the director.
     *  @exception IllegalActionException If the container cannot
     *   contain this director.
     *  @exception NameDuplicationException If the container already
     *   contains an Attribute with the same name.
     */
    public AmorphousDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Return a Receiver that can delegate to another receiver. */
    @Override
    public Receiver newReceiver() {
        return new DelegatingReceiver();
    }

    /** Inner class defining a receiver that can delegate to
     *  another receiver.
     */
    public static class DelegatingReceiver extends AbstractReceiver {

        /** The receiver to which to delegate. */
        private Receiver _receiver;

        /** Constructor. This creates the default SDFReceiver. */
        public DelegatingReceiver() {
            super();
            _receiver = new SDFReceiver();
        }

        /** Whenever the receiver is cleared, check the container
         *  to see whether a receiver class is specified. If it is,
         *  the create an instance of the receiver whose class name
         *  is given, replacing the current receiver.
         */
        @Override
        public void clear() throws IllegalActionException {
            IOPort container = getContainer();
            if (container != null) {
                StringParameter receiverClass = (StringParameter) container
                        .getAttribute("receiverClass", StringParameter.class);
                if (receiverClass != null) {
                    String className = ((StringToken) receiverClass.getToken())
                            .stringValue();
                    if (!className.equals(_receiver.getClass().toString())) {
                        try {
                            Class desiredClass = Class.forName(className);
                            _receiver = (Receiver) desiredClass.newInstance();
                        } catch (Exception e) {
                            throw new IllegalActionException(container, e,
                                    "Invalid class for receiver: " + className);
                        }
                    }
                }
            }
            _receiver.clear();
        }

        /** Delegate to the specified receiver. */
        @Override
        public Token get() throws NoTokenException {
            return _receiver.get();
        }

        /** Delegate to the specified receiver. */
        @Override
        public boolean hasRoom() {
            return _receiver.hasRoom();
        }

        /** Delegate to the specified receiver. */
        @Override
        public boolean hasRoom(int numberOfTokens) {
            return _receiver.hasRoom(numberOfTokens);
        }

        /** Delegate to the specified receiver. */
        @Override
        public boolean hasToken() {
            return _receiver.hasToken();
        }

        /** Delegate to the specified receiver. */
        @Override
        public boolean hasToken(int numberOfTokens) {
            return _receiver.hasToken(numberOfTokens);
        }

        /** Delegate to the specified receiver. */
        @Override
        public void put(Token token) throws NoRoomException,
        IllegalActionException {
            _receiver.put(token);
        }
    }
}
