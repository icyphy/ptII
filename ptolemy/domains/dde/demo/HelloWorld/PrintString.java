/* A demo actor that prints the value of each StringToken
 that it consumes.

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

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.StringToken;
import ptolemy.domains.dde.kernel.DDEActor;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// PrintString

/**
 A demo actor that prints the value of each StringToken
 that it consumes.

 @author John S. Davis II
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (davisj)
 @Pt.AcceptedRating Red (cxh)
 */
public class PrintString extends DDEActor {
    /** Construct a PrintString actor with the specified container and
     *  name.
     * @param container The container of this actor.
     * @param name The name of this actor.
     * @exception IllegalActionException If the constructor of the
     *  superclass throws an IllegalActionException.
     * @exception NameDuplicationException If the constructor of the
     *  superclass throws a NameDuplicationException .
     */
    public PrintString(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _input = new TypedIOPort(this, "input", true, false);
        _input.setMultiport(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute this actor by consuming StringTokens and
     *  then printing the corresponding String value.
     * @exception IllegalActionException If there is an error
     *  in executing getNextToken().
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        StringToken token = null;
        Time time;

        while (true) {
            token = (StringToken) getNextToken();
            time = getDirector().getModelTime();

            if (token == null) {
                System.out.println("Null token in PrintString");
            } else {
                System.out.println("\t" + token.toString() + "\tTime is "
                        + time);
            }
        }
    }

    /** Indicate that this actor's execution is complete by
     *  printing a corresponding statement.
     * @exception IllegalActionException If there is an error
     *  in the superclass method.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        System.out.println("\nIt is finished.\n");
        super.wrapup();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    public TypedIOPort _input;
}
