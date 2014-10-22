/* An adapter class that creates exceptions in CG.

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

package ptolemy.cg.kernel.generic;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;

//
//////////////////////////////////////////////////////////////////////////
////CGException

/**
 Create exceptions in code generation.

<p>This class is introduced so that we don't need to care whether the
object is a Nameable of not.  {@link ptolemy.kernel.util.IllegalActionException}
normally expects a Nameable as an argument.

@author Bert Rodiers
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (rodiers)
@Pt.AcceptedRating Red (rodiers)
 */

public class CGException {
    /** Throw an exception with a detail message.
     *  @param string The message.
     *  @exception IllegalActionException Always thrown by this method.
     */
    public static void throwException(String string)
            throws IllegalActionException {
        throwException(null, null, string);
    }

    /** Throw an exception with a detail message that includes the
     *  name of the first argument.
     *  @param component The component.
     *  @param string The message.
     *  @exception IllegalActionException Always thrown by this method.
     */
    public static void throwException(Object component, String string)
            throws IllegalActionException {
        throwException(component, null, string);
    }

    /** Throw an exception with a detail message that includes the
     *  name of the first argument.
     *  @param component The component.
     *  @param cause The cause of this exception, or null if the cause
     *  is not known or nonexistent.
     *  @param detail The message.
     *  @exception IllegalActionException Always thrown by this method.
     */
    public static void throwException(Object component, Throwable cause,
            String detail) throws IllegalActionException {
        if (component instanceof Nameable) {
            throw new IllegalActionException((Nameable) component, cause,
                    detail);
        } else {
            throw new IllegalActionException(null, cause, detail);
        }
    }
}
