/* Output the elapsed time in seconds.

@Copyright (c) 1998-2003 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

                                                PT_COPYRIGHT_VERSION 2
                                                COPYRIGHTENDKEY
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.data.DoubleToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
Upon firing, this actor outputs the elapsed real time in seconds
since the invocation of its initialize() method.  The output type is double.
The resolution of time depends on the implementation of the Java
virtual machine, but with Sun's JDK 1.3 under Windows 2000, it is
10 milliseconds.

@author  Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public class WallClockTime extends Source {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public WallClockTime(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        output.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Output the elapsed time in seconds since the invocation
     *  of the initialize() method.
     *  @exception IllegalActionException If there is no director, or
     *   if the base class throws it.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        output.broadcast(new DoubleToken(_getCurrentTime()));
    }

    /** Record the start time.
     *  @exception IllegalActionException If the base class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _startTime = System.currentTimeMillis();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    protected double _getCurrentTime() {
        long elapsedTime = System.currentTimeMillis() - _startTime;
        return (((double)elapsedTime)/1000.0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The start time. */
    private long _startTime;
}
