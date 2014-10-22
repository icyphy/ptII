/* A demo actor that outputs a StringToken for each real token
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

import java.util.LinkedList;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Punctuation

/**
 A demo actor that outputs a StringToken for each real token
 that it consumes.

 @author John S. Davis II
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (davisj)
 @Pt.AcceptedRating Red (cxh)
 */
public class Punctuation extends StringOut {
    /** Construct a Punctuation actor with the specified container and
     *  name.
     * @param container The container of this actor.
     * @param name The name of this actor.
     * @exception IllegalActionException If the constructor of the
     *  superclass throws an IllegalActionException.
     * @exception NameDuplicationException If the constructor of the
     *  superclass throws a NameDuplicationException .
     */
    public Punctuation(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _punctuation = new LinkedList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set up the string values that this actor will output.
     */
    @Override
    public LinkedList setUpStrings() {
        _punctuation.addLast("!");

        _punctuation.addLast("!");

        _punctuation.addLast(" ");

        _punctuation.addLast(" ");

        _punctuation.addLast(" ");

        _punctuation.addLast(" ");

        _punctuation.addLast(" ");

        _punctuation.addLast(" ");

        _punctuation.addLast(".");

        return _punctuation;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private LinkedList _punctuation;
}
