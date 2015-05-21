/* Attribute that regulates the passage of time to wait for real time to catch up.

 Copyright (c) 2007-2014 The Regents of the University of California.
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
package ptolemy.actor.lib;

import ptolemy.actor.AbstractInitializableAttribute;
import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.TimeRegulator;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// SynchronizeToRealTime

/**
 Attribute that regulates the passage of time to wait for real time to catch up.
 * The scaleFactor parameter is here to set up how many seconds in wallclock 
 * time should pass for a single second in model time. So a scale factor of 0.5 
 * will make the model time passes twice as fast as real time, while make it 
 * equals 2 means that 2 seconds in wallclock are needed for a single unit 
 * of time in the model.
 * 
 * The default value is 1.
 * 
 @author Edward A. Lee, Gilles Lasnier, Patricia Derler
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class SynchronizeToRealTime extends AbstractInitializableAttribute
	implements TimeRegulator {

    /** Construct an instance of the attribute.
     * @param container The container.
     * @param name The name.
     * @exception IllegalActionException If the superclass throws it.
     * @exception NameDuplicationException If the superclass throws it.
     */
    public SynchronizeToRealTime(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        scaleFactor = new Parameter(this,"scaleFactor");
        scaleFactor.setDisplayName("Time scale factor");
        scaleFactor.setTypeEquals(BaseType.DOUBLE);
        scaleFactor.setExpression("1.0");
        attributeChanged(scaleFactor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Propose a time to advance to.
     *  @param proposedTime The proposed time.
     *  @return The proposed time or a smaller time.
     *  @exception IllegalActionException If this attribute is not
     *   contained by an Actor.
     */
    @Override
    public Time proposeTime(Time proposedTime) throws IllegalActionException {
        NamedObj container = getContainer();
        if (!(container instanceof Actor)) {
            throw new IllegalActionException(this,
                    "SynchronizeToRealTime has to be contained by an Actor");
        }
        
        Director director = ((Actor) container).getDirector();
        Object mutexLockObject = director.mutexLockObject();

        int depth = 0;       
        try {
            synchronized (mutexLockObject) {
                while (true) {
                    
                    // NOTE: We assume that the elapsed time can be
                    // safely cast to a double.  This means that
                    // the SR domain has an upper limit on running
                    // time of Double.MAX_VALUE milliseconds.
                    double elapsedTime_s = director.elapsedTimeSinceStart() / 1000.0;
                    double currentTime_s = director.getModelTime().getDoubleValue();
                    
                    double scale = ((DoubleToken) scaleFactor.getToken()).doubleValue();
                    if (currentTime_s*scale <= elapsedTime_s) {
                        break;
                    }

                    long timeToWait_ms = (long) ((currentTime_s*scale - elapsedTime_s) * 1000.0);
                    
                    if (_debugging) {
                        _debug("Waiting for real time to pass: " + timeToWait_ms+ 
                                " before " + proposedTime.getDoubleValue());
                    }
                    
                    try {
                        // NOTE: The built-in Java wait() method
                        // does not release the
                        // locks on the workspace, which would block
                        // UI interactions and may cause deadlocks.
                        // SOLUTION: explicitly release read permissions.
                        if (timeToWait_ms > 0) {
                            // Bug fix from J. S. Senecal:
                            //
                            //  The problem was that sometimes, the
                            //  method Object.wait(timeout) was called
                            //  with timeout = 0. According to java
                            //  documentation:
                            //
                            // " If timeout is zero, however, then
                            // real time is not taken into
                            // consideration and the thread simply
                            // waits until notified."
                            depth = _workspace.releaseReadPermission();
                            mutexLockObject.wait(timeToWait_ms);
                        }
                    } catch (InterruptedException ex) {
                        // Continue executing.
                    }
                }
            }
        } finally {
            if (depth > 0) {
                _workspace.reacquireReadPermission(depth);
            }
        }
        return proposedTime;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////
    
    public Parameter scaleFactor;
}
