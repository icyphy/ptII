/* A CSP actor that continually performs conditional rendezvous in
 an alternating fashion with its input and output ports.

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
package ptolemy.domains.csp.demo.BusContention;

import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.domains.csp.kernel.CSPActor;
import ptolemy.domains.csp.kernel.ConditionalBranch;
import ptolemy.domains.csp.kernel.ConditionalReceive;
import ptolemy.domains.csp.kernel.ConditionalSend;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Memory

/**
 A CSP actor that continually performs conditional rendezvous in
 an alternating fashion with its input and output ports. The Memory
 actor can optionally send tokens with string values during
 conditional rendezvous.

 @author John S. Davis II
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Red (davisj)
 @Pt.AcceptedRating Red (cxh)
 */
public class Memory extends CSPActor {
    /** Construct a Memory actor with the specified container
     *  and name.
     * @param cont The container of this actor.
     * @param name The name of this actor.
     * @exception IllegalActionException If the actor cannot be
     *  contained by the proposed container.
     * @exception NameDuplicationException If the container
     *  already has an actor with this name.
     */
    public Memory(CompositeEntity cont, String name)
            throws IllegalActionException, NameDuplicationException {
        super(cont, name);

        input = new TypedIOPort(this, "input", true, false);
        output = new TypedIOPort(this, "output", false, true);

        input.setMultiport(true);
        output.setMultiport(true);

        input.setTypeEquals(BaseType.STRING);
        output.setTypeEquals(BaseType.GENERAL);

        _strValue = "initialValue";
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port. The type of this port is BaseType.STRING.
     *  This is a multiport.
     */
    public TypedIOPort input;

    /** The output port. The type of this port is BaseType.GENERAL.
     *  This is a multiport.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute this actor indefinitely unless there is an error
     *  during one of the conditional rendezvous attempts.
     * @exception IllegalActionException If there is an error
     *  during communication via the ports.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (_numInChannels == -1) {
            _numInChannels = 0;

            Receiver[][] rcvrs = input.getReceivers();

            for (Receiver[] rcvr : rcvrs) {
                for (int j = 0; j < rcvr.length; j++) {
                    _numInChannels++;
                }
            }
        }

        if (_numOutChannels == -1) {
            _numOutChannels = 0;

            Receiver[][] rcvrs = output.getRemoteReceivers();

            for (Receiver[] rcvr : rcvrs) {
                for (int j = 0; j < rcvr.length; j++) {
                    _numOutChannels++;
                }
            }
        }

        StringToken token;

        while (true) {
            token = new StringToken(_strValue);

            int numBranches = _numInChannels + _numOutChannels;
            ConditionalBranch[] branches = new ConditionalBranch[numBranches];

            // Receive Branches
            for (int i = 0; i < _numInChannels; i++) {
                branches[i] = new ConditionalReceive(true, input, i, i);
            }

            // Send Branches
            for (int i = 0; i < _numOutChannels; i++) {
                branches[i + _numInChannels] = new ConditionalSend(true,
                        output, i, i + _numInChannels, token);
            }

            int br = chooseBranch(branches);

            // Sleep so that graphical displays involving this
            // applet will pause after colors are changed.
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new TerminateProcessException(this, "Terminated");
            }

            if (br >= 0 && br < _numInChannels) {
                token = (StringToken) branches[br].getToken();
                _strValue = token.toString();
            } else if (br >= _numInChannels && br < numBranches) {
                _strValue = "write";
            } else if (br == -1) {
                return;
            }
        }
    }

    /** Return the value of the string that is associated with
     *  the input token.
     * @return The String value of the input token.
     */
    public synchronized String getString() {
        return _strValue;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
    private int _numInChannels = -1;

    private int _numOutChannels = -1;

    private String _strValue = null;
}
