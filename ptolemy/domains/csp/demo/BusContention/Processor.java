/* Processor

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
import ptolemy.data.Token;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.BooleanToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import java.util.Enumeration;
import collections.LinkedList;
import java.awt.event.*;


//////////////////////////////////////////////////////////////////////////
//// Processor
/**

@author John S. Davis II
@version $Id$

*/

public class Processor extends CSPActor {

    /**
     */
    public Processor(TypedCompositeActor cont, String name, int code)
            throws IllegalActionException, NameDuplicationException {
        super(cont, name);

        _requestOut = new TypedIOPort(this, "requestOut", false, true);
        _requestIn = new TypedIOPort(this, "requestIn", true, false);
        _memoryOut = new TypedIOPort(this, "memoryOut", false, true);
        _memoryIn = new TypedIOPort(this, "memoryIn", true, false);

        _requestOut.setTypeEquals(IntToken.class);
        _requestIn.setTypeEquals(BooleanToken.class);
        _memoryOut.setTypeEquals(StringToken.class);
        _memoryIn.setTypeEquals(Token.class);

        _code = code;

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
    public void accessMemory(boolean read) throws IllegalActionException {

        // State 1
        generateEvents( new ExecEvent( this, 1 ) );
        {
	    // generateEvents( new ExecEvent( this, 1 ) );
            if( _topGraphic != null ) {
                _topGraphic.receiveEvent(this, 1);
            }
        }
        double delayTime = java.lang.Math.random();
        if( delayTime < 0.25 ) {
            delayTime = 2.5;
        } else if ( delayTime >= 0.25 && delayTime < 0.5 ) {
            delayTime = 5.0;
        } else if ( delayTime >= 0.5 && delayTime < 0.75 ) {
            delayTime = 7.5;
        } else {
            delayTime = 10.0;
        }
        delay( delayTime );
        IntToken iToken = new IntToken( _code );
        _requestOut.broadcast(iToken);

        // State 2
        generateEvents( new ExecEvent( this, 2 ) );
	try {
	    Thread.sleep(300);
	} catch( InterruptedException e ) {
	    e.printStackTrace();
            throw new RuntimeException(e.toString());
	}
        BooleanToken bToken = (BooleanToken)_requestIn.get(0);

        if( bToken.booleanValue() ) {
            // State 3
            generateEvents( new ExecEvent( this, 3 ) );
	    try {
	        Thread.sleep(300);
	    } catch( InterruptedException e ) {
		e.printStackTrace();
		throw new RuntimeException(e.toString());
	    }
            if( read ) {
                _memoryIn.get(0);
            }
            else {
                StringToken strToken = new StringToken( getName() );
                _memoryOut.broadcast(strToken);
            }
            return;
        } else {
            // State 4
	    generateEvents( new ExecEvent( this, 4 ) );
	    try {
	        Thread.sleep(300);
	    } catch( InterruptedException e ) {
		e.printStackTrace();
		throw new RuntimeException(e.toString());
	    }
	}

        accessMemory(read);
    }

    /**
     */
    public boolean endYet() {
        double time = _dir.getCurrentTime();
        if( time > 50.0 ) {
            return true;
        }
        return false;
    }

    /**
     */
    public void fire() throws IllegalActionException {
        while(true) {
            if( performReadNext() ) {
                accessMemory(true);
            } else {
                accessMemory(false);
            }
            if( endYet() ) {
                return;
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
    public void initialize() throws IllegalActionException {
        super.initialize();
        TypedCompositeActor ca = (TypedCompositeActor)getContainer();
        _dir = (CSPDirector)ca.getDirector();
    }

    /**
     */
    public boolean performReadNext() {
        if( java.lang.Math.random() < 0.5 ) {
            return true;
        }
        return false;
    }

    /**
     */
    public void removeListeners(ExecEventListener listener) {
        if( _listeners == null ) {
            return;
        }
        _listeners.removeOneOf(listener);
    }

    /**
     */
    public void setGraphicFrame(BusContentionGraphic bcg)
            throws IllegalActionException {
        if( bcg == null ) {
            throw new IllegalActionException( this,
                    "BusContentionGraphic is null");
        }
        _topGraphic = bcg;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private TypedIOPort _requestIn;
    private TypedIOPort _requestOut;
    private TypedIOPort _memoryIn;
    private TypedIOPort _memoryOut;

    private int _code;

    private CSPDirector _dir;

    private LinkedList _listeners;
    private BusContentionGraphic _topGraphic;
}
