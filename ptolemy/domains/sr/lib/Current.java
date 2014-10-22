/* Output the most recent input received.

 Copyright (c) 2004-2014 The Regents of the University of California.
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
package ptolemy.domains.sr.lib;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Current

/**
 * Output the most recent input received.
 *
 * <p>At each tick of the clock, this actor outputs the most recently received
 * non-absent input. If no input has been received, then the output is absent.
 * This implements the Lustre "current" operator.
 *
 * <p>P. Caspi, D. Pilaud, N. Halbwachs, and J. A. Plaice, "LUSTRE: A
 * Declarative Language for Programming Synchronous Systems,"
 * Conference Record of the 14th Annual ACM Symp. on Principles of
 * Programming Languages, Munich, Germany, January, 1987.
 *
 * @author Paul Whitaker, Christopher Hylands, Edward A. Lee
 * @version $Id$
 @since Ptolemy II 4.1
 * @Pt.ProposedRating Yellow (cxh)
 * @Pt.AcceptedRating Red (cxh) Should support multiports
 */
public class Current extends Transformer {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Current(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If there is a token on the input port, consume exactly one token
     *  from the input port, and output this token.  If there is no token
     *  on the input port, output the most recent token received, if any.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            _lastInput = input.get(0);
        }

        if (_lastInput != null) {
            output.send(0, _lastInput);
        }
    }

    /** Initialize the buffer variable.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void initialize() throws IllegalActionException {
        _lastInput = null;
        super.initialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The most recent token received.
    private Token _lastInput;
}
