/* An actor that delays the the execution of the model until a specified time.

 Copyright (c) 2006-2014 The Regents of the University of California.
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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import ptolemy.actor.Director;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// DelayStart

/**
 This actor, which has no ports, delays the execution of the model
 until a specified time by sleeping the thread that calls the
 initialize() method until the specified time. If the time is
 in the past, then there is no delay. The time is assumed to be
 today, and is given as in "1:00 pm" for one in the afternoon.
 <p>
 If this actor is used in SDF, then the director must be set
 to allow disconnected graphs.  Note that it makes no sense
 to use this actor in PN or CSP, since it will only delay the
 start of its own thread.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2

 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class DelayStart extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DelayStart(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        startTime = new StringParameter(this, "startTime");
        startTime.setExpression("12:00 pm");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The start time. FIXME: Spec.
     *  This is a string that defaults to FIXME
     */
    public StringParameter startTime;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Sleep until the specified time.
     *  @exception IllegalActionException If the parent class throws it,
     *   or if the time cannot be parsed.
     */
    @Override
    public void initialize() throws IllegalActionException {
        DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);

        try {
            while (true) {
                Date date = dateFormat.parse(startTime.stringValue());

                // Unfortunately, in the above, if no date is given,
                // then the time is assumed to be on January 1, 1970.
                // Detect this, and reset the date to today.
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                if (calendar.get(Calendar.YEAR) == 1970) {
                    // Assume the date wasn't set.
                    Calendar now = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, now.get(Calendar.YEAR));
                    calendar.set(Calendar.DAY_OF_MONTH,
                            now.get(Calendar.DAY_OF_MONTH));
                    calendar.set(Calendar.MONTH, now.get(Calendar.MONTH));
                    date = calendar.getTime();
                }

                if (_debugging) {
                    _debug("Delaying start until: " + date);
                }

                long time = date.getTime();
                Director director = getDirector();
                long current = System.currentTimeMillis();

                if (time <= current) {
                    break;
                }

                // Synchronizing on the director here is incorrect.
                // See Workspace.wait(Object)
                // synchronized (director) {
                try {
                    _workspace.wait(director, time - current);
                } catch (InterruptedException e) {
                    // Ignore and continue;
                }
                // }
            }
        } catch (ParseException e) {
            throw new IllegalActionException(this, "Invalid startTime: "
                    + startTime.stringValue());
        }

        super.initialize();
    }
}
