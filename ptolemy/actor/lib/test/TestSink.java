/* A sink actor for testing.

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
*/

package ptolemy.actor.lib.test;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// TestSink
/**
A sink actor for testing.
This actor reads a token from its input port on each firing. It returns
the last token read in getToken().
The type of the input port is undeclared. This means that this actor
can accept any token on its input.

@author Yuhong Xion
@version $Id$
*/

public class TestSink extends TypedAtomicActor {

    /** Construct a TestSink.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TestSink(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

	_input = new TypedIOPort(this, "Input", true, false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read a token from the input port.
     *  @exception IllegalActionException Not thrown in this class.
     */
    public void fire()
	    throws IllegalActionException {
        _token = _input.get(0);
    }

    /** Return the token read in during the last firing.
     *  @return A Token.
     */
    public Token getToken() {
	return _token;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Token _token = null;
    private TypedIOPort _input = null;
}

