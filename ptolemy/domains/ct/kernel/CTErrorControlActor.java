/* Interface for actors that can used to control local error.

 Copyright (c) 1998 The Regents of the University of California.
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

import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// CTErrorControlActor
/**
Interface for Error Contol actors. Any actors that would like to control
the local error of an iteration should implement this interface. Usually
these actors, when the error is tolerable, will suggest a new step
size. For those actors that don't want to suggest new step size should
return twice the current step size.
@author Jie Liu
@version $Id$
*/
public interface CTErrorControlActor {

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Return true if current step is successful.
     */
    public boolean isSuccessful();

    /** Return the suggested next step size.For those actors that don't
     *  want to suggest new step size should return <B>twice</B>
     *  the current step size.
     */
    public double suggestedNextStepSize();

}
