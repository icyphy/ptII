/*
Below is the copyright agreement for the Ptolemy II system.
Version: $Id$

Copyright (c) 2007-2014 The Regents of the University of California.
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
 */
package doc.tutorial.domains;

import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.domains.pn.kernel.PNDirector;
import ptolemy.domains.pn.kernel.PNQueueReceiver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** This director modifies the PNDirector to support
 *  nonblocking reads. Specifically, it modifies the
 *  receiver so that hasToken() returns true only if the
 *  receiver has a token, unlike the original PNReceiver,
 *  where hasToken() always returns true. The price we
 *  pay for this flexibility is that models are no longer
 *  determinate.
 *  @author Edward A. Lee
 */
public class NondogmaticPNDirector extends PNDirector {

    /** Constructor. A director is an Attribute.
     *  @param container The container for the director.
     *  @param name The name of the director.
     *  @exception IllegalActionException If the container cannot
     *   contain this director.
     *  @exception NameDuplicationException If the container already
     *   contains an Attribute with the same name.
     */
    public NondogmaticPNDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Return a new instance of the specialized receiver used by
     *  this director.
     */
    @Override
    public Receiver newReceiver() {
        return new FlexibleReceiver();
    }

    /** Inner class defining the specialized receiver used by
     *  this director. This receiver overrides hasToken() to
     *  "tell the truth" about whether a token is present.
     */
    public static class FlexibleReceiver extends PNQueueReceiver {

        /** Override the base class to return true only if the
         *  receiver actually has a token.
         */
        @Override
        public boolean hasToken() {
            IOPort port = getContainer();
            Attribute attribute = port.getAttribute("tellTheTruth");
            if (attribute == null) {
                return super.hasToken();
            }
            // Tell the truth...
            return _queue.size() > 0;
        }
    }
}
