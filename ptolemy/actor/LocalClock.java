/* A clock that keeps track of model time at a level of the model hierarchy.

 Copyright (c) 1999-2010 The Regents of the University of California.
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
package ptolemy.actor;

import ptolemy.actor.util.Time;
import ptolemy.kernel.util.NamedObj;

/** A clock that keeps track of model time at a level of the model hierarchy.

 @author Ilge Akkaya, Patricia Derler, Edward A. Lee, Christos Stergiou, Michael Zimmer
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating yellow (eal)
 @Pt.AcceptedRating red (eal)
 */

public class LocalClock {

    /** Create a local clock. 
     *  @param director The associated director.
     */
    public LocalClock(Director director) {
        _director = director;
        
        // Make sure getCurrentTime() never returns null.
        _currentTime = Time.NEGATIVE_INFINITY;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public method                     ////

    /** Get current time. If it has never been set, then this will return
     *  Time.NEGATIVE_INFINITY.
     */
    public Time getCurrentTime() {
        return _currentTime;
    }
    
    /** Set current time.
     *  @param time The new local time.
     */
    public void setCurrentTime(Time time) {
        _currentTime = time;
    }

    /** If the associated director has an enclosing executive director,
     *  then make the local time of this clock equal to that of the
     *  executive director. Otherwise, do nothing.
     */
    public void synchronizeToEnvironmentTime() {
        NamedObj container = _director.getContainer();
        if (container instanceof Actor) {
            Director executiveDirector = ((Actor) container)
                    .getExecutiveDirector();

            if (executiveDirector != null) {
                Time outTime = executiveDirector.getModelTime();
                _currentTime = outTime;
            }
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variable                  ////

    /** The current time of this clock. */
    private Time _currentTime;
    
    /** The associated director. */
    private Director _director;
}
