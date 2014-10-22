/* An abstract class that is used to produce StringTokens on the
 output for each token that is consumed on the input.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.domains.dde.demo.HelloWorld;

import java.util.LinkedList;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// StringOut

/**
 An abstract class that is used to produce StringTokens on the
 output for each token that is consumed on the input.

 @author John S. Davis II
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (davisj)
 @Pt.AcceptedRating Red (cxh)
 */
public abstract class StringOut extends TypedAtomicActor {
    /** Construct a StringOut actor with the specified container and
     *  name.
     * @param container The container of this actor.
     * @param name The name of this actor.
     * @exception IllegalActionException If the constructor of the
     *  superclass throws an IllegalActionException.
     * @exception NameDuplicationException If the constructor of the
     *  superclass throws a NameDuplicationException .
     */
    public StringOut(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.STRING);
        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.STRING);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
    public TypedIOPort output;

    public TypedIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** This abstract method should be overridden to set up
     *  the string values that will be output as StringTokens
     *  when fire() is called.
     */
    public abstract LinkedList setUpStrings();

    /** Initialize this actor by setting the string values
     *  that will be output from this actor during calls
     *  to fire().
     * @exception IllegalActionException If there is an error
     *  in the superclass.
     */
    @Override
    public void initialize() throws IllegalActionException {
        _contents = setUpStrings();
        super.initialize();
    }

    /** Execute this actor by consuming a token and then producing
     *  a StringToken.
     * @exception IllegalActionException If there is an error
     *  when checking token availability in the input port.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            input.get(0);

            String string = (String) _contents.get(_cntr);
            output.broadcast(new StringToken(string));
            _cntr++;

            if (_cntr == _contents.size()) {
                _cntr = 0;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private LinkedList _contents;

    private int _cntr = 0;
}
