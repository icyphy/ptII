/* Construct a test hierarchal graph using the ptolemy.kernel classes.

 Copyright (c) 1997-2000 The Regents of the University of California.
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
@ProposedRating Red
@AcceptedRating Red
*/

package ptolemy.kvm.kernel.util.test;

import ptolemy.kvm.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// TestNamedObj.java
/**
Test out the NamedObj class 
@author Christopher Hylands
@version $Id$
*/
public class TestNamedObj {

    /** Construct the graph.
     */
    public TestNamedObj() throws IllegalActionException {
	_message = new String("In the constructor\n"); 
	PtolemyThread ptolemyThread = new PtolemyThread();
	_message = _message.concat("after PtolemyThread\n");
	Workspace workspace = new Workspace();
	_message = _message.concat("after Workspace\n");
	NamedObj N1 = new NamedObj();
	_message = _message.concat("after NamedObj\n");
	String name2 = new String ("N2");
	NamedObj N2 = new NamedObj(name2);
	if (name2.compareTo(N2.getName()) != 0 ) {
	    throw new InternalErrorException("names did not match");
	}
	_message = _message.concat("NamedObj.getName = " + N2.getName() +"\n");
	_message = _message.concat("after NamedObj\n");
  }

    public String getString() {
	return _message;
    }

    public static void main(String args[]) throws IllegalActionException {
        TestNamedObj exsys = new TestNamedObj();
    }

    protected String _message = null;
}
