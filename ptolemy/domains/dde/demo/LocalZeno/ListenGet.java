/* ListenGet is a test class used to test the consumption of tokens
and check relevant parameters.

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
@AcceptedRating Red (davisj@eecs.berkeley.edu)

*/

package ptolemy.domains.dde.demo.LocalZeno;

import ptolemy.actor.*;
import ptolemy.actor.gui.*;
import ptolemy.domains.dde.kernel.*;
import ptolemy.domains.dde.kernel.test.*;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.InternalErrorException;
import java.util.Enumeration;
import collections.LinkedList;


//////////////////////////////////////////////////////////////////////////
//// ListenGet
/**
ListenGet is a test class used to test the consumption of tokens
and check relevant parameters. ListenGet can retrieve N tokens
where 'N' is set in the constructor. For each token retrieved, the
current time at the time of consumption and the actual consumed
token can be queried. The queries can take place after the completion
of Manager.run().


@author John S. Davis II
@version $Id$

*/

public class ListenGet extends DDEGetNToken {

    /**
     */
    public ListenGet(TypedCompositeActor cont, String name, int numTokens)
            throws IllegalActionException, NameDuplicationException {
        super(cont, name, numTokens);
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
    public boolean prefire() throws IllegalActionException {
	generateEvents( new ExecEvent( this, 1 ) );
	try {
	    Thread.sleep(100);
	} catch(InterruptedException e) {
            throw new InternalErrorException( "Error with "
            	    + "sleeping thread in prefire");
	}
	return super.prefire();
    }

    /**
     */
    public boolean postfire() throws IllegalActionException {
	generateEvents( new ExecEvent( this, 2 ) );
	try {
	    Thread.sleep(100);
	} catch(InterruptedException e) {
            throw new InternalErrorException( "Error with "
            	    + "sleeping thread in postfire");
	}
	return super.postfire();
    }

    /**
     */
    public void wrapup() throws IllegalActionException {
	generateEvents( new ExecEvent( this, 3 ) );
	super.wrapup();
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
    ////                         private variables                 ////

    private LinkedList _listeners;

}
