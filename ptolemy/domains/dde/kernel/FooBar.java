/* 

 Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Yellow (davisj@eecs.berkeley.edu)
@AcceptedRating Yellow (yuhong@eecs.berkeley.edu)

*/

package ptolemy.domains.dde.kernel;

import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// FooBar
/**

@author John S. Davis II
@version $Id$
*/
public class FooBar {

    /** 
     */
    public FooBar(String classMethodName, String methodLocation) {
	this(null, classMethodName, methodLocation, -100.0, null);
    }

    /** 
     */
    public FooBar(String callee, String classMethodName, String methodLocation) {
	this(callee, classMethodName, methodLocation, -100.0, null);
    }

    /** 
     */
    public FooBar(String callee, String classMethodName, String methodLocation,
	    double time, Token token) {
	// System.out.println(_callee+":\t###FooBar starting construction###");
	if( callee != null ) {
	    _callee = callee;
	}
	_classMethodName = classMethodName;
	_methodLocation = methodLocation;
	_time = time;

	Thread thread = Thread.currentThread();
	DDEThread ddeThread = null;
	if( thread instanceof DDEThread ) {
	    ddeThread = (DDEThread)thread;
	    _caller = ((Nameable)ddeThread.getActor()).getName();
	    if( _time == -100.0 ) {
		_time = ddeThread.getTimeKeeper().getCurrentTime();
	    }
	}

	String tokenType = null;
	if( token instanceof NullToken ) {
	    tokenType = "NullToken";
	} else {
	    tokenType = "RealToken";
	}
	String msg = _callee;

	if( classMethodName.endsWith(".put()") ) {
	    msg += ":\t" + _methodLocation + " of " + _classMethodName; 
	    msg += " was called by \"" + _caller; 
	    msg += "\" with a " + tokenType + " at time " + _time;
	} else {
	    msg += ":\t" + _methodLocation + " of " + _classMethodName; 
	    msg += " was called by \"" + _caller; 
	    msg += "\" at time " + _time;
	}


	// System.out.println(msg);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    ///////////////////////////////////////////////////////////////////
    ////                   package friendly methods		   ////

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    private String _caller = "null";
    private String _callee = "null";
    private String _classMethodName = "null";
    private String _methodLocation = "null";
    private double _time = -100;

}




