/* A source actor for testing.

 Copyright (c) 1998 The Regents of the University of California.
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
A source actor for testing.

@author Yuhong Xion
@version $Id$
*/

public class TestSource extends TypedAtomicActor {

    /** Construct a testSource.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TestSource(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

	_output = new TypedIOPort(this, "Output", false, true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Send out the contained token.
     *  @exception IllegalActionException Not thrown in this class.
     */
    public void fire()
	    throws IllegalActionException {
	_output.broadcast(_token);
    }

    /** Set the specified token into this actor and set the declared
     *  type.
     */
    public void setToken(Token t) {
	_token = t;
	_output.setDeclaredType(_token.getClass());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Token _token = new IntToken(0);
    private TypedIOPort _output = null;
}

