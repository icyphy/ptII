/* RTMaude Code generator helper class for the NonInterruptibleTimer class.

 Copyright (c) 2009-2011 The Regents of the University of California.
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
package ptolemy.codegen.rtmaude.domains.de.lib;

//////////////////////////////////////////////////////////////////////////
//// NonInterruptibleTimer

/**
 * Generate RTMaude code for a NonInterruptibleTimer in DE domain.
 *
 * @see ptolemy.domains.de.lib.NonInterruptibleTimer
 * @author Kyungmin Bae
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating red (kquine)
 * @Pt.AcceptedRating red (kquine)
 * @deprecated Use ptolemy.actor.lib.ResettableTimer.
 */
public class NonInterruptibleTimer extends Timer {
    /**
     * Constructor method for the NonInterruptibleTimer adapter.
     * @param component The associated NonInterruptibleTimer
     */
    public NonInterruptibleTimer(
            ptolemy.domains.de.lib.NonInterruptibleTimer component) {
        super(component);
    }
}
