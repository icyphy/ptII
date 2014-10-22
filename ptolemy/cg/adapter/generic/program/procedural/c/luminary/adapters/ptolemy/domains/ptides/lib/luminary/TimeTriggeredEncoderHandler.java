/* Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.c.luminary.adapters.ptolemy.domains.ptides.lib.luminary;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * A code generation adapter class for
 * ptolemy.domains.ptides.lib.targets.luminary.TimeTriggeredEncoderInputDevice. This actor
 * is basically the same as GPInputHandler, only the C template includes more
 * calculations.
 * @author Jia Zou
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Yellow (jiazou)
 * @Pt.AcceptedRating Red (jiazou)
 */

public class TimeTriggeredEncoderHandler extends GPInputHandler {
    /** Construct an adapter with the given
     *  ptolemy.domains.ptides.lib.TimeTriggeredEncoderInputDevice actor.
     *  @param actor The given ptolemy.domains.ptides.lib.targets.luminary.TimeTriggeredEncoderInputDevice actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TimeTriggeredEncoderHandler(
            ptolemy.domains.ptides.lib.luminary.TimeTriggeredEncoderHandler actor)
            throws IllegalActionException, NameDuplicationException {
        super(actor);
    }
}
