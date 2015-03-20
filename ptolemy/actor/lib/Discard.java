/* A simple sink actor that consumes and discards input tokens.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.lib;

import java.net.URL;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Configurable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Discard

/**
 A simple sink actor that consumes and discards input tokens.
 <p>This actor is useful in situations where the value of an output is not
 needed, but for some reason, the output cannot be left unconnected.
 Also, when manipulating bus signals, this actor is useful if values
 in the middle of the bus need to be discarded.  Leaving the bus
 unconnected in the middle will not work because no channel is allocated
 to an unconnected relation in a bus.

 <p>If a model has backward type propagation enabled, then either
 use {@link ptolemy.actor.lib.DiscardDoubles} or
 {@link ptolemy.actor.lib.DiscardDoublesArray} or set the 
 type of the input port of the Discard actor accordingly.</p>

 @see ptolemy.actor.lib.DiscardDoubles
 @see ptolemy.actor.lib.DiscardDoublesArray
 @author Edward A. Lee, contributors: Christopher Brooks, Brian Hudson
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Yellow (ssachs)
 */
public class Discard extends Sink implements Configurable {

    /** Construct an actor with an input multiport.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Discard(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Do nothing, as this actor is not actually configurable.
     *  <p>This class implements {@link ptolemy.kernel.util.Configurable}
     *  so that the MoML filters can replace graphical actors that
     *  implement Configurable with this actor.  Note that the actors
     *  to be replaced must have an input port named <i>input</i>, so
     *  not all Configurable actors can be replaced with this actor.
     *  @param base Ignored.
     *  @param source Ignored.
     *  @param text Ignored.
     *  @exception Exception Not thrown in this base class.
     */
    @Override
    public void configure(URL base, String source, String text)
            throws Exception {
    }

    /** Read one token from each input channel and discard it.
     *  If there is no input on a channel, then skip that channel, doing
     *  nothing with it.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        int width = input.getWidth();

        for (int i = 0; i < width; i++) {
            if (input.hasToken(i)) {
                input.get(i);
            }
        }
    }

    /** Return null because this actor is not actually configurable.
     *  @return Always return null.
     */
    @Override
    public String getConfigureSource() {
        return null;
    }

    /** Return null because this actor is not actually configurable.
     *  @return Always return null.
     */
    @Override
    public String getConfigureText() {
        return null;
    }
}
