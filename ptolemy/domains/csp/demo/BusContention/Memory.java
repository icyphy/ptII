/* Memory

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

import ptolemy.domains.csp.lib.*;
import ptolemy.domains.csp.kernel.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.Token;
import ptolemy.data.StringToken;


//////////////////////////////////////////////////////////////////////////
//// Memory
/**

@author John S. Davis II
@version $Id$

*/

public class Memory extends CSPActor {

    /**
     */
    public Memory(TypedCompositeActor cont, String name)
            throws IllegalActionException, NameDuplicationException {
        super(cont, name);

        _input = new TypedIOPort(this, "input", true, false);
        _output = new TypedIOPort(this, "output", false, true);

        _input.setMultiport(true);
        _output.setMultiport(true);

        _input.setTypeEquals(StringToken.class);
        _output.setTypeEquals(Token.class);

        _strValue = "initialValue";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public void fire() throws IllegalActionException {

        if( _numInChannels == -1 ) {
            _numInChannels = 0;
            Receiver[][] rcvrs = _input.getReceivers();
            for( int i = 0; i < rcvrs.length; i++ ) {
                for( int j = 0; j < rcvrs[i].length; j++ ) {
                    _numInChannels++;
                }
            }
        }
        if( _numOutChannels == -1 ) {
            _numOutChannels = 0;
            Receiver[][] rcvrs = _output.getRemoteReceivers();
            for( int i = 0; i < rcvrs.length; i++ ) {
                for( int j = 0; j < rcvrs[i].length; j++ ) {
                    _numOutChannels++;
                }
            }
        }

        boolean beginning = true;
        StringToken token;

        while(true) {
            token = new StringToken( _strValue );

            int numBranches = _numInChannels + _numOutChannels;
            ConditionalBranch[] branches = new ConditionalBranch[numBranches];

            // Receive Branches
            for( int i=0; i < _numInChannels; i++ ) {
                branches[i] = new
                    ConditionalReceive(true, _input, i, i);
            }

            // Send Branches
            for( int i=0; i < _numOutChannels; i++ ) {
                branches[i+_numInChannels] = new
                    ConditionalSend(true, _output, i, i+_numInChannels, token);
            }

            int br = chooseBranch( branches );

	    // Sleep so that graphical displays involving this
	    // applet will pause after colors are changed.
	    try {
	        Thread.sleep(300);
	    } catch( InterruptedException e ) {
	        System.err.println("InterruptedException during Thread.sleep()");
		e.printStackTrace();
	    }

            if( br >= 0 && br < _numInChannels ) {
                token = (StringToken)branches[br].getToken();
                _strValue = token.stringValue();
            } else if ( br >= _numInChannels && br < numBranches ) {
                _strValue = "write";
            } else if( br == -1 ) {
                return;
            }
        }
    }

    public synchronized String getString() {
        return _strValue;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    private TypedIOPort _input;
    private TypedIOPort _output;

    private int _numInChannels = -1;
    private int _numOutChannels = -1;

    private String _strValue = null;
}
