/* Code generator adapter class associated with the PtidesBasicReceiver class.

 Copyright (c) 2009-2014 The Regents of the University of California.
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

package ptolemy.cg.adapter.generic.program.procedural.c.renesas.adapters.ptolemy.domains.ptides.kernel;

import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
////PtidesBasicReceiver

/** The adapter for ptides basic receiver.
 *  @author Jia Zou, Man-Kit Leung, Isaac Liu
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (jiazou)
 *  @Pt.AcceptedRating Red (jiazou)
 */
@Deprecated
public class PtidesBasicReceiver
        extends
        ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.ptides.kernel.PtidesBasicReceiver {

    /** Construct a ptides basic receiver.
     *  @param receiver The ptolemy.domains.ptides.kernel.PtidesBasicReceiver
     *  that corresponds with this adapter.
     *  @exception IllegalActionException If throw by the superclass.
     */
    public PtidesBasicReceiver(
            ptolemy.domains.ptides.kernel.PtidesReceiver receiver)
            throws IllegalActionException {
        super(receiver);
    }

}
