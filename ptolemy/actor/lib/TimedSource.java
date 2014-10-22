/* Base class for time-based sources.

 Copyright (c) 1998-2014 The Regents of the University of California.
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

import ptolemy.actor.Director;
import ptolemy.actor.TimedActor;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// TimedSource

/**
 Base class for time-based sources.  A time-based source is
 a source where the output value is a function of current time.
 For some sequence-based domains, such as SDF, actors of this type
 probably do not make sense because current time is not incremented.
 This actor has a parameter, <i>stopTime</i>, that optionally controls
 the duration for which the actor is fired. When current time reaches
 the stopTime, postfire() returns false. This indicates
 to the director that this actor should not be invoked again.
 The default value of stopTime is <i>Infinity</i>, which results in postfire
 always returning true.  In other words, this makes the lifetime
 infinite. Derived classes must call super.postfire() for this mechanism to
 work.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (bilung)
 */
public class TimedSource extends Source implements TimedActor {
    /** Construct an actor with the given container and name.
     *  The <i>stopTime</i> parameter is also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TimedSource(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        stopTime = new Parameter(this, "stopTime");
        stopTime.setExpression("Infinity");
        stopTime.setTypeEquals(BaseType.DOUBLE);

        stopTimeIsLocal = new Parameter(this, "stopTimeIsLocal");
        stopTimeIsLocal.setTypeEquals(BaseType.BOOLEAN);
        stopTimeIsLocal.setExpression("false");

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-20\" y=\"-20\" " + "width=\"40\" height=\"40\" "
                + "style=\"fill:lightGrey\"/>\n"
                + "<circle cx=\"0\" cy=\"0\" r=\"17\""
                + "style=\"fill:white\"/>\n"
                + "<line x1=\"0\" y1=\"-15\" x2=\"0\" y2=\"-13\"/>\n"
                + "<line x1=\"0\" y1=\"14\" x2=\"0\" y2=\"16\"/>\n"
                + "<line x1=\"-15\" y1=\"0\" x2=\"-13\" y2=\"0\"/>\n"
                + "<line x1=\"14\" y1=\"0\" x2=\"16\" y2=\"0\"/>\n"
                + "<line x1=\"0\" y1=\"-8\" x2=\"0\" y2=\"0\"/>\n"
                + "<line x1=\"0\" y1=\"0\" x2=\"11.26\" y2=\"-6.5\"/>\n"
                + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The time at which postfire() should return false. This is a
     *  double that defaults to Infinity, which means that postfire()
     *  never returns false (or at least, doesn't do so due to stopTime
     *  having been exceeded).
     */
    public Parameter stopTime;

    /** If true, use the local time to compare against the <i>stopTime</i>
     *  parameter, rather than the global time. Local time may differ
     *  from global time inside modal models and certain domains
     *  that manipulate time. This is a boolean that defaults
     *  to false.
     */
    public Parameter stopTimeIsLocal;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the <i>stopTime</i> parameter is changed and the model is
     *  executing, then if the new value is greater
     *  than zero and greater than the current time, then ask the director
     *  to fire this actor at that time.  If the new value is less than
     *  the current time, then request refiring at the current time.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == stopTime) {
            double newStopTimeValue = ((DoubleToken) stopTime.getToken())
                    .doubleValue();

            if (_executing) {
                Time newStopTime = new Time(getDirector(), newStopTimeValue);
                Director director = getDirector();

                if (director != null) {
                    Time currentTime;
                    boolean localTime = ((BooleanToken) stopTimeIsLocal
                            .getToken()).booleanValue();
                    if (localTime) {
                        currentTime = director.getModelTime();
                    } else {
                        currentTime = director.getGlobalTime();
                    }

                    if (newStopTime.compareTo(currentTime) > 0) {
                        // NOTE: Do not throw an exception if the director ignores this
                        // stop time request or returns some other value of time.
                        // postfire() will return false on the next firing after time
                        // equals the stop time or exceeds it.
                        director.fireAt(this, newStopTime);
                    } else {
                        /* Do not throw an exception here because it makes it
                         * impossible to change the stop time after the model has run.
                         *
                        throw new IllegalActionException(this, "The stop time "
                                + newStopTime
                                + " is earlier than the current time "
                                + currentTime);
                         */
                    }
                }

                _stopTime = newStopTime;
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Get the stop time.
     *  @return The stop time.
     *  @deprecated As of Ptolemy II 4.1, replaced by
     *  {@link #getModelStopTime}
     */
    @Deprecated
    public double getStopTime() {
        return getModelStopTime().getDoubleValue();
    }

    /** Get the stop time.
     *  @return The stop time.
     */
    public Time getModelStopTime() {
        return _stopTime;
    }

    /** Initialize the actor. Schedule a refiring of this actor at the
     *  stop time given by the <i>stopTime</i> parameter.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        Director director = getDirector();

        if (director == null) {
            throw new IllegalActionException(this, "No director!");
        }

        double stopTimeValue = ((DoubleToken) stopTime.getToken())
                .doubleValue();
        _stopTime = new Time(getDirector(), stopTimeValue);

        Time currentTime;
        boolean localTime = ((BooleanToken) stopTimeIsLocal.getToken())
                .booleanValue();
        if (localTime) {
            currentTime = director.getModelTime();
        } else {
            currentTime = director.getGlobalTime();
        }

        if (!_stopTime.isInfinite() && _stopTime.compareTo(currentTime) > 0) {
            // NOTE: Do not throw an exception if the director ignores this
            // stop time request or returns some other value of time.
            // postfire() will return false on the next firing after time
            // equals the stop time or exceeds it.
            director.fireAt(this, _stopTime);
            _executing = true;
        }
    }

    /** Return false if the current time is greater than or equal to
     *  the <i>stopTime</i> parameter value.
     *  Otherwise, return true.  Derived classes should call this
     *  at the end of their postfire() method and return its returned
     *  value.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (!super.postfire()) {
            // Presumably, stopRequested is true.
            return false;
        }
        Time currentTime;
        boolean localTime = ((BooleanToken) stopTimeIsLocal.getToken())
                .booleanValue();
        if (localTime) {
            currentTime = getDirector().getModelTime();
        } else {
            currentTime = getDirector().getGlobalTime();
        }

        if (currentTime.compareTo(_stopTime) >= 0) {
            return false;
        }

        return true;
    }

    /** Return false if the current time is greater than or equal to
     *  the <i>stopTime</i> parameter value.
     *  Otherwise, return what the superclass returns.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        Boolean result = super.prefire();
        Time currentTime;
        boolean localTime = ((BooleanToken) stopTimeIsLocal.getToken())
                .booleanValue();
        if (localTime) {
            currentTime = getDirector().getModelTime();
        } else {
            currentTime = getDirector().getGlobalTime();
        }
        if (currentTime.compareTo(_stopTime) >= 0) {
            return false;
        }
        return result;
    }

    /** Override the base class to reset a flag that indicates that the
     *  model is executing. This method is invoked exactly once per execution
     *  of an application.  None of the other action methods should be
     *  be invoked after it.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _executing = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Flag indicating that the model is running.
    private boolean _executing = false;

    // stop time.
    private Time _stopTime;
}
