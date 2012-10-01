/* Interface for directors with a period parameter.

 Copyright (c) 2000-2012 The Regents of the University of California.
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

package ptolemy.actor.util;

import ptolemy.actor.Executable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;

///////////////////////////////////////////////////////////////////
//// PeriodicDirector

/**
 Interface for directors that have a <i>period</i> parameter.
 These directors are, by default, untimed, but become timed when
 the <i>period</i> parameter is given a value other than 0.0.
 The <i>period</i> parameter, if non-zero, specifies the
 amount of model time that elapses per iteration.
 In this case, the director should fire only at times
 that are multiples of the <i>period</i>. If the director is
 at the top level, then it is responsible for incrementing time
 between iterations. Otherwise, it is responsible for invoking
 fireAt() on the enclosing executive director to request
 subsequent firings, and refusing to fire (by returning false
 from prefire()) at times that are not multiples of the <i>period</i>.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public interface PeriodicDirector extends Executable, Nameable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if this director is embedded inside an opaque composite
     *  actor contained by another composite actor. Note that some classes,
     *  such as RunCompositeActor, may return false even if they are actually
     *  embedded, but they want to be treated as if they were not.
     *  @return True if this directory is embedded inside an opaque composite
     *  actor contained by another composite actor.
     */
    public boolean isEmbedded();

    /** Return the value of the period as a double.
     *  @return The value of the period as a double.
     *  @exception IllegalActionException If the period parameter
     *   cannot be evaluated
     */
    public double periodValue() throws IllegalActionException;
}
