/* Exception thrown on an attempt to send an illegal output to a port.

 Copyright (c) 1997-2005 The Regents of the University of California.
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

import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.Nameable;

//////////////////////////////////////////////////////////////////////////
//// IllegalOutputException

/**
 Thrown on an attempt to send a value to a port that violates the output
 monotonicity constraint of the SR domain.

 @author Paul Whitaker
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Green (pwhitake)
 @Pt.AcceptedRating Green (pwhitake)
 @see ptolemy.domains.sr.kernel.SRDirector
 @see ptolemy.domains.sr.kernel.SRReceiver
 */
public class IllegalOutputException extends KernelRuntimeException {
    /** Constructs an Exception with a detail message.
     *  @param detail The message.
     */
    public IllegalOutputException(String detail) {
        this(null, detail);
    }

    /** Constructs an Exception with a detail message that is only the
     *  name of the argument.
     *  @param object The object.
     */
    public IllegalOutputException(Nameable object) {
        this(object, null);
    }

    /** Constructs an Exception with a detail message that includes the
     *  name of the first argument.
     *  @param object The object.
     *  @param detail The message.
     */
    public IllegalOutputException(Nameable object, String detail) {
        super(object, detail);
    }
}
