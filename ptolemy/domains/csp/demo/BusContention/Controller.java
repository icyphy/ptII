/* Controller

 Copyright (c) 1998-1999 The Regents of the University of California.
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

*/

package ptolemy.domains.csp.demo.BusContention;

import ptolemy.actor.*;
import ptolemy.actor.gui.*;
import ptolemy.actor.process.*;
import ptolemy.domains.csp.lib.*;
import ptolemy.domains.csp.kernel.*;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.Token;
import ptolemy.data.IntToken;
import ptolemy.data.BooleanToken;
import java.util.Enumeration;
import collections.LinkedList;


//////////////////////////////////////////////////////////////////////////
//// Controller
/**


@author John S. Davis II
@version $Id$

*/

public class Controller extends CSPActor {

    /**
     */
    public Controller(TypedCompositeActor cont, String name)
            throws IllegalActionException, NameDuplicationException {
        super(cont, name);

        _requestOut = new TypedIOPort(this, "requestOut", false, true);
        _requestIn = new TypedIOPort(this, "requestIn", true, false);
        _contendOut = new TypedIOPort(this, "contendOut", false, true);
        _contendIn = new TypedIOPort(this, "contendIn", true, false);

        _requestOut.setMultiport(true);
        _requestIn.setMultiport(true);

        _requestOut.setTypeEquals(BooleanToken.class);
        _requestIn.setTypeEquals(IntToken.class);
        _contendOut.setTypeEquals(Token.class);
        _contendIn.setTypeEquals(Token.class);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public void addListeners(ExecEventListener listener) {
        if( _listeners == null ) {
            _listeners = new LinkedList();
        }
        _listeners.insertLast(listener);
    }

    /**
     */
    public void fire() throws IllegalActionException {

        if( _numRequestInChannels == -1 ) {
            _numRequestInChannels = 0;
            Receiver[][] rcvrs = _requestIn.getReceivers();
            for( int i = 0; i < rcvrs.length; i++ ) {
                for( int j = 0; j < rcvrs[i].length; j++ ) {
                    _numRequestInChannels++;
                }
            }
        }

        int br;
        int code;
        BooleanToken posAck = new BooleanToken( true );
        BooleanToken negAck = new BooleanToken( false );

        while(true) {

            //
            // State 1: Wait for 1st Request
            //
            generateEvents( new ExecEvent( this, 1 ) );
	    // System.out.println("\t\t\tSTATE 1: " +getName());
            ConditionalBranch[] requiredBranches =
                new ConditionalBranch[_numRequestInChannels];
            for( int i = 0; i < _numRequestInChannels; i++ ) {
                requiredBranches[i] = new
                    ConditionalReceive(true, _requestIn, i, i);
            }

            br = chooseBranch(requiredBranches);

            if( br != -1 ) {
                IntToken token = (IntToken)requiredBranches[br].getToken();
                code = token.intValue();
                _winningPortChannelCode =
                    new PortChannelCode(_requestIn, br, code);
            }


            //
            // State 2: Notify Contention Alarm of 1st Request
            //
            generateEvents( new ExecEvent( this, 2 ) );
	    // System.out.println("\t\t\tSTATE 2: " +getName());
            _contendOut.send(0, new Token() );


            //
            // State 3: Wait for Contenders and Send Ack's
            //
            generateEvents( new ExecEvent( this, 3 ) );
	    // System.out.println("\t\t\tSTATE 3: " +getName());
            _losingPortChannelCodes = new LinkedList();
            boolean continueCDO = true;
            while( continueCDO ) {
                requiredBranches =
                    new ConditionalBranch[_numRequestInChannels+1];
                for( int i = 0; i < _numRequestInChannels; i++ ) {
                    requiredBranches[i] =
                        new ConditionalReceive(true, _requestIn, i, i);
                }
                int j = _numRequestInChannels;
                requiredBranches[j] =
                    new ConditionalReceive(true, _contendIn, 0, j);

                br = chooseBranch(requiredBranches);


                // Contention Occurred...and might happen again
                if( br >= 0 && br < _numRequestInChannels ) {
                    IntToken token = (IntToken)requiredBranches[br].getToken();
                    code = token.intValue();
                    if( code > _winningPortChannelCode.getCode() ) {
                        _losingPortChannelCodes.
                            insertFirst(_winningPortChannelCode);
                        _winningPortChannelCode =
                            new PortChannelCode(_requestIn, br, code);
                    } else {
                        _losingPortChannelCodes.insertFirst( new
                                PortChannelCode(_requestIn, br, code) );
                    }

                } else if( br == _numRequestInChannels ) {

                    //
                    // State 4: Contention is Over
                    //
                    generateEvents( new ExecEvent( this, 4 ) );
	            // System.out.println("\t\t\tSTATE 4: " +getName());

                    requiredBranches[br].getToken();

                    // Send Positive Ack
                    int ch =  _winningPortChannelCode.getChannel();
                    _requestOut.send(ch, posAck);

                    // Send Negative Ack
                    Enumeration enum = _losingPortChannelCodes.elements();
                    PortChannelCode pcc = null;
                    while( enum.hasMoreElements() ) {
                        pcc = (PortChannelCode)enum.nextElement();
                        ch = pcc.getChannel();
                        _requestOut.send(ch, negAck);
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

    /**
     */
    public void generateEvents(ExecEvent event) {
        if( _listeners == null ) {
            return;
        }
        Enumeration enum = _listeners.elements();
        while( enum.hasMoreElements() ) {
            ExecEventListener newListener =
                (ExecEventListener)enum.nextElement();
            newListener.stateChanged(event);
        }
    }

    /**
     */
    public void removeListeners(ExecEventListener listener) {
        if( _listeners == null ) {
            return;
        }
        _listeners.removeOneOf(listener);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    private TypedIOPort _requestIn;
    private TypedIOPort _requestOut;
    private TypedIOPort _contendIn;
    private TypedIOPort _contendOut;

    private int _numRequestInChannels = -1;

    private PortChannelCode _winningPortChannelCode;
    private LinkedList _losingPortChannelCodes;

    private LinkedList _listeners;
}
