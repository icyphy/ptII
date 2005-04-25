/* Exception to indicate numerically nonconvergence.

Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.domains.ct.kernel;

import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.Nameable;


//////////////////////////////////////////////////////////////////////////
//// NumericalNonconvergenceException

/**
   This exception is used to indicate a numerical iteration not converging,
   typically in the process of finding the fixed point solution of an
   algebraic equation. This is a RuntimeException.
   @author  Jie Liu
   @version $Id$
   @since Ptolemy II 0.2
   @deprecated As Ptolemy II 4.1, this class is not used any more.
   @Pt.ProposedRating Green (hyzheng)
   @Pt.AcceptedRating Green (hyzheng)
*/
public class NumericalNonconvergeException extends InvalidStateException {
    /** Constructs an Exception with only a detail message.
     *  @param detail The message.
     */
    public NumericalNonconvergeException(String detail) {
        super(detail);
    }

    /** Constructs an Exception with a detail message that includes the
     *  name of the first argument and the second argument string.
     *  @param obj The object.
     *  @param detail The message.
     */
    public NumericalNonconvergeException(Nameable obj, String detail) {
        super(obj, detail);
    }

    /** Constructs an Exception with a detail message that includes the
     *  names of the first two arguments plus the third argument string.
     *  @param obj1 The first object.
     *  @param obj2 The second object.
     *  @param detail The message.
     */
    public NumericalNonconvergeException(Nameable obj1, Nameable obj2,
        String detail) {
        super(obj1, obj2, detail);
    }
}
