/* The receiver for the SR domain.

 Copyright (c) 1998-2006 The Regents of the University of California.
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
package ptolemy.domains.sr.kernel;

import ptolemy.domains.fp.kernel.FPReceiver;

//////////////////////////////////////////////////////////////////////////
//// SRReceiver

/**

 The receiver for the Synchronous Reactive (SR) domain.  This
 receiver extends the {@link ptolemy.domains.fp.kernel.FPReceiver} class.

 @author Paul Whitaker, contributor: Christopher Hylands, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Green (pwhitake)
 @Pt.AcceptedRating Green (pwhitake)
 @see ptolemy.domains.sr.kernel.SRDirector
 */
public class SRReceiver extends FPReceiver {
    /** Construct an SRReceiver with unknown state and the given director.
     *  @param director The director of this receiver.
     */
    public SRReceiver(SRDirector director) {
        super(director);
    }
}
