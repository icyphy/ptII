/* A CSP actor that serves as a controller of a shared resource.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Red (davisj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.csp.demo.BusContention;

// Ptolemy imports.
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.domains.csp.kernel.CSPActor;
import ptolemy.domains.csp.kernel.ConditionalBranch;
import ptolemy.domains.csp.kernel.ConditionalReceive;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


//////////////////////////////////////////////////////////////////////////
//// Controller
/**
A CSP actor that serves as a controller of a shared resource. This
actor has four "informal" states that are cycled through in the
fire() method. In these four states this actor accepts and grants
requests for access to a shared resource. An Actor that wants to
request or be granted access to a shared resource must connect to
this controller via the requestInput and requestOutput ports. To
aid in monitoring such requests, the controller connects to a
ContentionAlarm actor through its contendInput and contendOutput
ports.

In state one the controller waits for requests on its requestInput
port. Once the first request has been received, the controller
moves to state two and sends a message to the ContentionAlarm
notifying it that a request has occurred at the current time. The
controller then moves to state three and performs a conditional
rendezvous on its contendInput and requestInput ports. Once an input
has been received from the ContentionAlarm, then the controller knows
that that the request contention period is over and it moves to state
four. In state four the controller notifies the resource requestors as
to whether their requests were granted based on priority relative to
other contenders.

@author John S. Davis II
@version $Id$
@since Ptolemy II 0.3
*/

public class Controller extends CSPActor {

    /** Construct a Controller actor with the specified container
     *  and name.
     * @param cont The container of this actor.
     * @param name The name of this actor.
     * @exception IllegalActionException If the actor cannot be
     *  contained by the proposed container.
     * @exception NameDuplicationException If the container
     *  already has an actor with this name.
     */
    public Controller(CompositeEntity cont, String name)
            throws IllegalActionException, NameDuplicationException {
        super(cont, name);

        requestOutput = new TypedIOPort(this, "requestOutput", false, true);
        requestInput = new TypedIOPort(this, "requestInput", true, false);
        contendOutput = new TypedIOPort(this, "contendOutput", false, true);
        contendInput = new TypedIOPort(this, "contendInput", true, false);

        requestOutput.setMultiport(true);
        requestInput.setMultiport(true);

        requestOutput.setTypeEquals(BaseType.BOOLEAN);
        requestInput.setTypeEquals(BaseType.INT);
        contendOutput.setTypeEquals(BaseType.GENERAL);
        contendInput.setTypeEquals(BaseType.GENERAL);

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The resource request input port. Resource requests are made
     *  through this port with a token that include's the requestor's
     *  priority level. The type of this port is BaseType.INT. This
     *  port is a multiport.
     */
    public TypedIOPort requestInput;

    /** The resource request output port. Resources are granted through
     *  this port. The type of this port is BaseType.BOOLEAN. This port
     *  is a multiport.
     */
    public TypedIOPort requestOutput;

    /** The contention input port. The availability of data on this
     *  port indicates that additional resource contention does not
     *  exist at the current time. The type of this port is
     *  BaseType.GENERAL.
     */
    public TypedIOPort contendInput;

    /** The contention output port. Output data on this port can be
     *  used to trigger a ContentionAlarm. The type of this port is
     *  BaseType.GENERAL.
     */
    public TypedIOPort contendOutput;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute this actor indefinitely.
     * @exception IllegalActionException If there is an error
     *  during communication through any of the input or output
     *  ports.
     */
    public void fire() throws IllegalActionException {

        if ( _numRequestInChannels == -1 ) {
            _numRequestInChannels = 0;
            Receiver[][] rcvrs = requestInput.getReceivers();
            for ( int i = 0; i < rcvrs.length; i++ ) {
                for ( int j = 0; j < rcvrs[i].length; j++ ) {
                    _numRequestInChannels++;
                }
            }
        }

        int br;
        int code;
        BooleanToken posAck = new BooleanToken( true );
        BooleanToken negAck = new BooleanToken( false );

        while (true) {

            //
            // State 1: Wait for 1st Request
            //
            _debug( new ExecEvent( this, ExecEvent.WAITING) );
            ConditionalBranch[] requiredBranches =
                new ConditionalBranch[_numRequestInChannels];
            for ( int i = 0; i < _numRequestInChannels; i++ ) {
                requiredBranches[i] = new
                    ConditionalReceive(true, requestInput, i, i);
            }

            br = chooseBranch(requiredBranches);

            if ( br != -1 ) {
                IntToken token = (IntToken)requiredBranches[br].getToken();
                code = token.intValue();
                _winningPortChannelCode =
                    new PortChannelCode(requestInput, br, code);
            }


            //
            // State 2: Notify Contention Alarm of 1st Request
            //
            contendOutput.send(0, new Token() );


            //
            // State 3: Wait for Contenders and Send Ack's
            //
            _debug( new ExecEvent( this, ExecEvent.ACCESSING) );
            _losingPortChannelCodes = new LinkedList();
            boolean continueCDO = true;
            while ( continueCDO ) {
                requiredBranches =
                    new ConditionalBranch[_numRequestInChannels+1];
                for ( int i = 0; i < _numRequestInChannels; i++ ) {
                    requiredBranches[i] =
                        new ConditionalReceive(true, requestInput, i, i);
                }
                int j = _numRequestInChannels;
                requiredBranches[j] =
                    new ConditionalReceive(true, contendInput, 0, j);

                br = chooseBranch(requiredBranches);


                // Contention Occurred...and might happen again
                if ( br >= 0 && br < _numRequestInChannels ) {
                    IntToken token = (IntToken)requiredBranches[br].getToken();
                    code = token.intValue();
                    if ( code > _winningPortChannelCode.getCode() ) {
                        _losingPortChannelCodes.
                            add(0, _winningPortChannelCode);
                        _winningPortChannelCode =
                            new PortChannelCode(requestInput, br, code);
                    } else {
                        _losingPortChannelCodes.add(0, new
                            PortChannelCode(requestInput, br, code) );
                    }

                } else if ( br == _numRequestInChannels ) {

                    //
                    // State 4: Contention is Over
                    //
                    _debug( new ExecEvent( this, ExecEvent.BLOCKED ) );

                    requiredBranches[br].getToken();

                    // Send Positive Ack
                    int ch =  _winningPortChannelCode.getChannel();
                    requestOutput.send(ch, posAck);

                    // Send Negative Ack
                    Iterator losingPorts = _losingPortChannelCodes.iterator();
                    PortChannelCode pcc = null;
                    while ( losingPorts.hasNext() ) {
                        pcc = (PortChannelCode)losingPorts.next();
                        ch = pcc.getChannel();
                        requestOutput.send(ch, negAck);
                    }

                    // Prepare to Wait for New Requests...enter state 1
                    continueCDO = false;
                    _winningPortChannelCode = null;
                    _losingPortChannelCodes = null;

                }


                // All branches failed.
                else {
                    continueCDO = false;
                    return;
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    private int _numRequestInChannels = -1;

    private PortChannelCode _winningPortChannelCode;
    private List _losingPortChannelCodes;
}
