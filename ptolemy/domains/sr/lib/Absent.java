/* An actor that makes its output value absent.

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
package ptolemy.domains.sr.lib;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Absent

/**
 An actor that makes its output value absent.  That is, it produces no
 tokens, and it calls the send(index, null) method of the output port on
 each firing.

 @author Paul Whitaker, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Red (pwhitake)
 */
public class Absent extends Undefined {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Absent(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        new Parameter(trigger, "_hide", BooleanToken.TRUE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return false. This actor can produce some output event the input
     *  receiver has status unknown.
     *
     *  @return False.
     */
    @Override
    public boolean isStrict() {
        return false;
    }

    /** Output an absent value by calling the send(0, null) method of the
     *  output port.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        output.send(0, null);
    }

    /** Return true. This actor always fires.
     *
     *  @return True.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        return true;
    }

    /** Override the base class to declare that the <i>output</i>
     *  port does not depend on the <i>trigger</i> port in a firing.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        removeDependency(trigger, output);
    }
}
